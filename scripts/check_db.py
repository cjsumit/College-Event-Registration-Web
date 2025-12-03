import sqlite3, os
p = os.path.abspath('registrations.db')
print('DB path:', p)
conn = sqlite3.connect(p)
cur = conn.cursor()
cur.execute("SELECT name FROM sqlite_master WHERE type='table'")
print('tables =', cur.fetchall())
try:
    cur.execute('SELECT id, student_name, event_name, tickets, email, phone, created_at FROM registrations')
    rows = cur.fetchall()
    print('rows count =', len(rows))
    for r in rows:
        print(r)
except Exception as e:
    print('error:', e)
conn.close()
