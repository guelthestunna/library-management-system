-- ============================================================
--  LIBRARY MANAGEMENT SYSTEM — Database Setup (FIXED)
--  Run this file in MySQL Workbench.
-- ============================================================

DROP DATABASE IF EXISTS library_db;
CREATE DATABASE library_db;
USE library_db;

-- ── Books ─────────────────────────────────────────────────
CREATE TABLE books (
    book_id          INT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    genre            VARCHAR(100),
    isbn             VARCHAR(20),
    total_copies     INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    added_date       DATE NOT NULL
);

-- ── Members ───────────────────────────────────────────────
CREATE TABLE members (
    member_id       INT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(20),
    address         TEXT,
    registered_date DATE NOT NULL
);

-- ── Checkouts ─────────────────────────────────────────────
CREATE TABLE checkouts (
    checkout_id   INT AUTO_INCREMENT PRIMARY KEY,
    book_id       INT NOT NULL,
    member_id     INT NOT NULL,
    checkout_date DATE NOT NULL,
    due_date      DATE NOT NULL,
    return_date   DATE,
    status        ENUM('BORROWED', 'RETURNED', 'OVERDUE') DEFAULT 'BORROWED',
    FOREIGN KEY (book_id)   REFERENCES books(book_id)     ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
);

-- ── Sample Data ───────────────────────────────────────────
INSERT INTO books (title, author, genre, isbn, total_copies, available_copies, added_date) VALUES
('Clean Code',                     'Robert C. Martin',   'Technology', '9780132350884', 3, 3, CURDATE()),
('The Pragmatic Programmer',       'David Thomas',       'Technology', '9780135957059', 2, 2, CURDATE()),
('Introduction to Algorithms',     'Thomas H. Cormen',   'Technology', '9780262033848', 2, 2, CURDATE()),
('Harry Potter and the Sorcerer',  'J.K. Rowling',       'Fiction',    '9780439708180', 5, 5, CURDATE()),
('To Kill a Mockingbird',          'Harper Lee',         'Fiction',    '9780061935466', 3, 3, CURDATE()),
('The Great Gatsby',               'F. Scott Fitzgerald','Classic',    '9780743273565', 2, 2, CURDATE()),
('Thinking Fast and Slow',         'Daniel Kahneman',    'Psychology', '9780374533557', 2, 2, CURDATE()),
('Atomic Habits',                  'James Clear',        'Self-Help',  '9780735211292', 4, 4, CURDATE());

INSERT INTO members (full_name, email, phone, address, registered_date) VALUES
('Juan dela Cruz',  'juan@email.com',  '09171234567', 'Manila, PH',      CURDATE()),
('Maria Santos',    'maria@email.com', '09281234567', 'Quezon City, PH', CURDATE()),
('Jose Reyes',      'jose@email.com',  '09391234567', 'Caloocan, PH',    CURDATE());
