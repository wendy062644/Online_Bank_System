import sqlite3
from datetime import datetime

def create_transaction_database():
    """
    建立或更新交易紀錄的資料庫結構。
    """
    conn = sqlite3.connect('transactions.db')
    cursor = conn.cursor()

    try:
        # 創建交易紀錄表
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                transaction_time TEXT NOT NULL,
                member_id INTEGER NOT NULL,
                amount INTEGER NOT NULL,
                note TEXT
            )
        ''')
        conn.commit()
        print("Transaction database created successfully.")
    except Exception as e:
        print(f"Error creating transaction database: {e}")
    finally:
        conn.close()

def add_transaction(member_id, amount, note=""):
    """
    新增一筆交易紀錄。
    """
    conn = sqlite3.connect('transactions.db')
    cursor = conn.cursor()

    try:
        # 取得當前時間
        transaction_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

        # 插入交易紀錄
        cursor.execute('''
            INSERT INTO transactions (transaction_time, member_id, amount, note)
            VALUES (?, ?, ?, ?)
        ''', (transaction_time, member_id, amount, note))
        conn.commit()
        print(f"Transaction for member {member_id} added successfully.")
    except Exception as e:
        print(f"Error adding transaction: {e}")
    finally:
        conn.close()

def transfer_transaction(sender_id, recipient_id, amount, note=""):
    """
    記錄匯款交易：記錄扣款者與收款者的交易紀錄。
    """
    conn = sqlite3.connect('transactions.db')
    cursor = conn.cursor()

    try:
        transaction_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        
        # 插入轉帳者交易紀錄
        sender_note = f"Transfer to ID {recipient_id}"
        if note:
            sender_note += f" - {note}"
        cursor.execute('''
            INSERT INTO transactions (transaction_time, member_id, amount, note)
            VALUES (?, ?, ?, ?)
        ''', (transaction_time, sender_id, -amount, sender_note))

        # 插入收款者交易紀錄
        recipient_note = f"Transfer from ID {sender_id}"
        if note:
            recipient_note += f" - {note}"
        cursor.execute('''
            INSERT INTO transactions (transaction_time, member_id, amount, note)
            VALUES (?, ?, ?, ?)
        ''', (transaction_time, recipient_id, amount, recipient_note))

        conn.commit()
        print(f"Transfer recorded successfully: {sender_id} to {recipient_id}")
    except Exception as e:
        print(f"Error recording transfer: {e}")
    finally:
        conn.close()

def view_transactions():
    """
    檢視所有交易紀錄。
    """
    conn = sqlite3.connect('transactions.db')
    cursor = conn.cursor()

    try:
        cursor.execute('SELECT * FROM transactions')
        rows = cursor.fetchall()
        for row in rows:
            print(f"ID: {row[0]}, Time: {row[1]}, Member ID: {row[2]}, Amount: {row[3]}, Note: {row[4]}")
    except Exception as e:
        print(f"Error viewing transactions: {e}")
    finally:
        conn.close()

if __name__ == '__main__':
    # 創建交易資料庫
    create_transaction_database()

    # 測試存款、取款交易
    add_transaction(member_id=1, amount=500, note="Deposit")
    add_transaction(member_id=2, amount=-200, note="Withdrawal")

    # 測試匯款交易
    transfer_transaction(sender_id=1, recipient_id=2, amount=300, note="Payment for services")

    # 檢視交易紀錄
    view_transactions()
