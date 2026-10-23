-- ====================================================================
-- Cyber Crime Report and Resolution System (CCRRS)
-- Database Schema Script for MySQL 8.0
-- ====================================================================

DROP DATABASE IF EXISTS ccrrs_db;
CREATE DATABASE ccrrs_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ccrrs_db;

-- 1. Investigating Officers Table
CREATE TABLE officers (
    officer_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_number VARCHAR(30) UNIQUE NOT NULL,
    rank_title VARCHAR(50) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    active_cases INT DEFAULT 0
);

-- 2. Complaints Table
CREATE TABLE complaints (
    case_id VARCHAR(30) PRIMARY KEY,
    complainant_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    city VARCHAR(50) NOT NULL,
    category VARCHAR(100) NOT NULL,
    description TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    severity INT NOT NULL CHECK (severity BETWEEN 1 AND 10),
    financial_loss DECIMAL(15, 2) DEFAULT 0.00,
    victim_count INT DEFAULT 1,
    priority_score DOUBLE DEFAULT 0.0,
    status VARCHAR(30) DEFAULT 'REGISTERED',
    assigned_officer_id VARCHAR(20),
    deadline_days INT DEFAULT 5,
    investigation_cost INT DEFAULT 10,
    expected_resolution_value INT DEFAULT 50,
    FOREIGN KEY (assigned_officer_id) REFERENCES officers(officer_id) ON DELETE SET NULL
);

-- 3. Evidence Entities (Graph Vertices)
CREATE TABLE evidence_entities (
    entity_id VARCHAR(50) PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_type ENUM('SUSPECT', 'DEVICE_IMEI', 'IP_ADDRESS', 'BANK_ACCOUNT', 'PHONE_NUMBER') NOT NULL,
    metadata VARCHAR(255)
);

-- 4. Evidence Network Edges (Graph Adjacency)
CREATE TABLE network_edges (
    edge_id INT AUTO_INCREMENT PRIMARY KEY,
    source_entity_id VARCHAR(50) NOT NULL,
    target_entity_id VARCHAR(50) NOT NULL,
    relation_type VARCHAR(50) NOT NULL,
    case_id VARCHAR(30),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_entity_id) REFERENCES evidence_entities(entity_id) ON DELETE CASCADE,
    FOREIGN KEY (target_entity_id) REFERENCES evidence_entities(entity_id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES complaints(case_id) ON DELETE SET NULL
);

-- 5. Case Status Change History (Undo Stack Log)
CREATE TABLE status_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    case_id VARCHAR(30) NOT NULL,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    remarks VARCHAR(255),
    changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES complaints(case_id) ON DELETE CASCADE
);

-- ====================================================================
-- SEED DATA
-- ====================================================================

INSERT INTO officers (officer_id, name, badge_number, rank_title, specialization) VALUES
('OFF-101', 'Insp. Vikram Rathore', 'B-8801', 'Inspector', 'Digital Forensics & Reverse Engineering'),
('OFF-102', 'SI Sneha Kulkarni', 'B-9142', 'Sub-Inspector', 'Financial Cybercrime & UPI Tracing'),
('OFF-103', 'Insp. Arjun Mehta', 'B-7623', 'Inspector', 'Network Intrusion & Cyber Terrorism');

INSERT INTO complaints (case_id, complainant_name, phone, email, city, category, description, severity, financial_loss, victim_count, priority_score, status, assigned_officer_id, deadline_days, investigation_cost, expected_resolution_value) VALUES
('CCRRS-1001', 'Rajesh Sharma', '+919811223344', 'rajesh.s@example.com', 'Delhi', 'UPI & Banking Fraud', 'Fraudulent QR code sent over WhatsApp disguised as electricity bill payment', 7, 85000.00, 1, 57.0, 'REGISTERED', 'OFF-102', 4, 15, 78),
('CCRRS-1002', 'Dr. Anita Roy', '+919822334455', 'anita.roy@lifecare.org', 'Bengaluru', 'Hacking & Ransomware', 'Critical patient database locked with .locked ext requiring 5 BTC ransom', 10, 2500000.00, 50, 330.0, 'UNDER_INVESTIGATION', 'OFF-103', 2, 50, 98),
('CCRRS-1003', 'Priya Verma', '+919833445566', 'priya.v@example.com', 'Mumbai', 'Cyber Harassment & Defamation', 'Morphed images circulated on Instagram demanding extortion money', 8, 120000.00, 2, 76.0, 'REGISTERED', 'OFF-101', 3, 10, 82),
('CCRRS-1004', 'Amit Patel', '+919844556677', 'amit.patel@gujtrade.com', 'Ahmedabad', 'Phishing & Identity Theft', 'Airtel SIM deactivated remotely; Rs. 6.5L transferred via RTGS in 15 mins', 9, 650000.00, 4, 235.0, 'REGISTERED', 'OFF-102', 3, 25, 90),
('CCRRS-1005', 'Sunita Nair', '+919855667788', 'sunita.n@example.com', 'Kochi', 'UPI & Banking Fraud', 'Joined fake Apex Crypto Signals channel; promised 300% returns; funds frozen', 9, 1400000.00, 22, 271.0, 'UNDER_INVESTIGATION', 'OFF-102', 5, 35, 92),
('CCRRS-1006', 'Rohan Gupta', '+919866778899', 'rohan.g@example.com', 'Kolkata', 'Phishing & Identity Theft', 'Phishing email mimicking Amazon Diwali reward voucher stolen credentials', 5, 45000.00, 1, 35.0, 'REGISTERED', 'OFF-101', 7, 8, 60);

INSERT INTO evidence_entities (entity_id, entity_name, entity_type, metadata) VALUES
('SUSPECT-01', 'Vikram Choudhary @ ShadowV', 'SUSPECT', 'Mule Recruiter & Phisher'),
('SUSPECT-02', 'Sameer Khan @ ByteGhost', 'SUSPECT', 'Malware Author & Ransomware Operator'),
('SUSPECT-03', 'Anand Verma', 'SUSPECT', 'Social Media Extortionist'),
('IP-103.21.244.12', 'Delhi Broadband VPN Proxy', 'IP_ADDRESS', 'ISP: Spectra Delhi'),
('IP-45.118.65.201', 'Tor Exit Relay Node', 'IP_ADDRESS', 'Location: Netherlands Gateway'),
('IMEI-863920194829102', 'OnePlus 11 5G', 'DEVICE_IMEI', 'Primary Sim: Airtel'),
('DEVICE-SERVER-BGLR', 'Dell PowerEdge R740', 'DEVICE_IMEI', 'Compromised Hospital Server'),
('BANK-HDFC-9921', 'HDFC Lajpat Nagar AC-9921', 'BANK_ACCOUNT', 'Mule Account in Delhi'),
('BANK-ICICI-4412', 'ICICI Indiranagar AC-4412', 'BANK_ACCOUNT', 'Shell Trade Account'),
('PHONE-+919876543210', 'Airtel Delhi SIM', 'PHONE_NUMBER', 'Burner SIM card'),
('PHONE-+918765432109', 'Jio Mumbai SIM', 'PHONE_NUMBER', 'WhatsApp caller ID');

INSERT INTO network_edges (source_entity_id, target_entity_id, relation_type, case_id) VALUES
('SUSPECT-01', 'IP-103.21.244.12', 'USED_IP', 'CCRRS-1001'),
('SUSPECT-01', 'BANK-HDFC-9921', 'MULE_RECEIVER', 'CCRRS-1001'),
('IMEI-863920194829102', 'IP-103.21.244.12', 'DEVICE_LOGGED_IP', 'CCRRS-1004'),
('IMEI-863920194829102', 'BANK-HDFC-9921', 'TRANSACTION_TERMINAL', 'CCRRS-1004'),
('SUSPECT-02', 'IP-45.118.65.201', 'DEPLOYED_EXPLOIT_VIA', 'CCRRS-1002'),
('IP-45.118.65.201', 'DEVICE-SERVER-BGLR', 'INFILTRATED_TARGET', 'CCRRS-1002'),
('SUSPECT-02', 'BANK-ICICI-4412', 'RECEIVES_PROCEEDS', 'CCRRS-1005'),
('BANK-ICICI-4412', 'PHONE-+919876543210', 'OTP_REGISTERED_PHONE', 'CCRRS-1005'),
('SUSPECT-03', 'PHONE-+919876543210', 'CALLER_ORIGIN', 'CCRRS-1003'),
('SUSPECT-01', 'PHONE-+918765432109', 'DISPATCH_SMS_GATEWAY', 'CCRRS-1006');
