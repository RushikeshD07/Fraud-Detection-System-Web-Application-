from flask import Flask, render_template, request, redirect, url_for, flash
import sqlite3
import os
from datetime import datetime, timedelta

app = Flask(__name__)
app.secret_key = 'fraud_detection_secret_key'

DATABASE = 'fraud_detection.db'

def get_db():
    conn = sqlite3.connect(DATABASE)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    with app.app_context():
        db = get_db()
        with open('schema_sqlite.sql', 'r') as f:
            db.executescript(f.read())
        db.commit()

@app.route('/')
def home():
    return render_template('home.html')

@app.route('/users')
def users():
    db = get_db()
    users = db.execute('SELECT * FROM users ORDER BY user_id').fetchall()
    return render_template('users.html', users=users)

@app.route('/add_user', methods=['GET', 'POST'])
def add_user():
    if request.method == 'POST':
        name = request.form['name']
        email = request.form['email']
        balance = float(request.form.get('balance', 0))
        
        db = get_db()
        try:
            db.execute('INSERT INTO users (name, email, account_balance) VALUES (?, ?, ?)',
                      (name, email, balance))
            db.commit()
            flash('User added successfully!', 'success')
            return redirect(url_for('users'))
        except sqlite3.IntegrityError:
            flash('Email already exists!', 'error')
    
    return render_template('add_user.html')

@app.route('/transactions')
def transactions():
    db = get_db()
    txns = db.execute('''
        SELECT t.*, u.name as user_name 
        FROM transactions t 
        JOIN users u ON t.user_id = u.user_id 
        ORDER BY t.txn_timestamp DESC
    ''').fetchall()
    return render_template('transactions.html', transactions=txns)

@app.route('/add_transaction', methods=['GET', 'POST'])
def add_transaction():
    db = get_db()
    users = db.execute('SELECT user_id, name FROM users').fetchall()
    
    if request.method == 'POST':
        user_id = int(request.form['user_id'])
        amount = float(request.form['amount'])
        txn_type = request.form['txn_type']
        location = request.form['location']
        
        # Insert transaction
        cursor = db.execute('INSERT INTO transactions (user_id, amount, txn_type, location) VALUES (?, ?, ?, ?)',
                           (user_id, amount, txn_type, location))
        txn_id = cursor.lastrowid
        
        # Run fraud detection
        run_fraud_detection(db, txn_id, user_id, amount, txn_type, location)
        
        db.commit()
        flash('Transaction added successfully!', 'success')
        return redirect(url_for('transactions'))
    
    return render_template('add_transaction.html', users=users)

@app.route('/fraud_alerts')
def fraud_alerts():
    db = get_db()
    alerts = db.execute('''
        SELECT a.*, t.amount, t.txn_type, u.name as user_name
        FROM fraud_alerts a
        JOIN transactions t ON a.txn_id = t.txn_id
        JOIN users u ON t.user_id = u.user_id
        ORDER BY a.created_at DESC
    ''').fetchall()
    return render_template('fraud_alerts.html', alerts=alerts)

@app.route('/update_alert/<int:alert_id>', methods=['POST'])
def update_alert(alert_id):
    status = request.form['status']
    db = get_db()
    db.execute('UPDATE fraud_alerts SET status = ? WHERE alert_id = ?', (status, alert_id))
    db.commit()
    flash('Alert status updated!', 'success')
    return redirect(url_for('fraud_alerts'))

def run_fraud_detection(db, txn_id, user_id, amount, txn_type, location):
    reasons = []
    
    # Rule 1: High amount
    if amount > 50000:
        reasons.append(f"Transaction amount exceeds Rs. 50000")
    
    # Rule 2: Suspicious location
    suspicious_locations = ['Unknown', 'Dubai', 'Lagos', 'Karachi', 'Offshore']
    if location in suspicious_locations:
        reasons.append(f"Suspicious location: {location}")
    
    # Rule 3: Rapid transactions
    if is_rapid_transaction(db, user_id, txn_id):
        reasons.append(f"Multiple transactions within 5 minutes")
    
    if reasons:
        reason = "; ".join(reasons)
        db.execute('INSERT INTO fraud_alerts (txn_id, fraud_reason) VALUES (?, ?)', (txn_id, reason))
        print(f"[ALERT] FRAUD ALERT RAISED: {reason}")
    else:
        print("[OK] No fraud indicators found")

def is_rapid_transaction(db, user_id, current_txn_id):
    # Count transactions in last 5 minutes excluding current
    count = db.execute('''
        SELECT COUNT(*) FROM transactions 
        WHERE user_id = ? AND txn_id != ? 
        AND txn_timestamp >= datetime('now', '-5 minutes')
    ''', (user_id, current_txn_id)).fetchone()[0]
    return count >= 3

if __name__ == '__main__':
    if not os.path.exists(DATABASE):
        init_db()
    app.run(debug=True)