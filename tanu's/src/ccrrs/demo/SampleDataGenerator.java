package ccrrs.demo;

import ccrrs.algorithms.decision.BacktrackingSuspectSearch.SuspectCandidate;
import ccrrs.ds.graph.SuspectNetworkGraph;
import ccrrs.ds.linear.PostfixPriorityEvaluator;
import ccrrs.ds.linear.SinglyLinkedList;
import ccrrs.ds.tree.CategoryBinaryTree;
import ccrrs.ds.tree.ComplaintBST;
import ccrrs.model.Complaint;
import ccrrs.model.EvidenceEntity;
import ccrrs.model.EvidenceEntity.EntityType;
import ccrrs.model.Officer;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates sample cybercrime complaints, officers, graph networks,
 * and suspect candidate profiles for demo and automated testing.
 */
public class SampleDataGenerator {

    public static List<Complaint> createSampleComplaints() {
        List<Complaint> list = new ArrayList<>();

        // Case 1: UPI Phishing / QR Code scam
        Complaint c1 = new Complaint(
                "CCRRS-1001", "Rajesh Sharma", "+919811223344", "rajesh.s@example.com", "Delhi",
                "UPI & Banking Fraud", "Fraudulent QR code sent over WhatsApp disguised as electricity bill payment",
                "2026-09-01 10:15:00", 7, 85000.0, 1, 4, 15, 78);
        c1.addEvidenceEntityId("SUSPECT-01");
        c1.addEvidenceEntityId("IP-103.21.244.12");
        c1.addEvidenceEntityId("BANK-HDFC-9921");

        // Case 2: Hospital Ransomware Attack
        Complaint c2 = new Complaint(
                "CCRRS-1002", "Dr. Anita Roy", "+919822334455", "anita.roy@lifecare.org", "Bengaluru",
                "Hacking & Ransomware", "Critical patient database locked with .locked ext requiring 5 BTC ransom",
                "2026-09-02 02:40:00", 10, 2500000.0, 50, 2, 50, 98);
        c2.addEvidenceEntityId("SUSPECT-02");
        c2.addEvidenceEntityId("IP-45.118.65.201");
        c2.addEvidenceEntityId("DEVICE-SERVER-BGLR");

        // Case 3: Deepfake Blackmail & Defamation
        Complaint c3 = new Complaint(
                "CCRRS-1003", "Priya Verma", "+919833445566", "priya.v@example.com", "Mumbai",
                "Cyber Harassment & Defamation", "Morphed images circulated on Instagram demanding extortion money",
                "2026-09-03 14:20:00", 8, 120000.0, 2, 3, 10, 82);
        c3.addEvidenceEntityId("PHONE-+919876543210");
        c3.addEvidenceEntityId("SUSPECT-03");

        // Case 4: SIM Swap & Netbanking Drain
        Complaint c4 = new Complaint(
                "CCRRS-1004", "Amit Patel", "+919844556677", "amit.patel@gujtrade.com", "Ahmedabad",
                "Phishing & Identity Theft", "Airtel SIM deactivated remotely; Rs. 6.5L transferred via RTGS in 15 mins",
                "2026-09-04 18:05:00", 9, 650000.0, 4, 3, 25, 90);
        c4.addEvidenceEntityId("BANK-HDFC-9921"); // Shared mule account with Case 1!
        c4.addEvidenceEntityId("IP-103.21.244.12"); // Shared IP with Case 1!
        c4.addEvidenceEntityId("IMEI-863920194829102");

        // Case 5: Telegram Fake Crypto Investment Scam
        Complaint c5 = new Complaint(
                "CCRRS-1005", "Sunita Nair", "+919855667788", "sunita.n@example.com", "Kochi",
                "UPI & Banking Fraud", "Joined fake 'Apex Crypto Signals' channel; promised 300% returns; funds frozen",
                "2026-09-05 11:30:00", 9, 1400000.0, 22, 5, 35, 92);
        c5.addEvidenceEntityId("SUSPECT-02"); // Cross-linked with Ransomware suspect!
        c5.addEvidenceEntityId("BANK-ICICI-4412");

        // Case 6: E-commerce Gift Card Theft
        Complaint c6 = new Complaint(
                "CCRRS-1006", "Rohan Gupta", "+919866778899", "rohan.g@example.com", "Kolkata",
                "Phishing & Identity Theft", "Phishing email mimicking Amazon Diwali reward voucher stolen credentials",
                "2026-09-06 16:45:00", 5, 45000.0, 1, 7, 8, 60);
        c6.addEvidenceEntityId("SUSPECT-01");
        c6.addEvidenceEntityId("PHONE-+918765432109");

        // Compute priority score for each complaint using the Postfix Evaluator
        for (Complaint c : list = List.of(c1, c2, c3, c4, c5, c6)) {
            double priority = PostfixPriorityEvaluator.computeCasePriority(
                    c.getSeverity(), c.getFinancialLoss(), c.getVictimCount());
            c.setPriorityScore(priority);
        }

        return new ArrayList<>(list);
    }

    public static List<Officer> createSampleOfficers() {
        List<Officer> officers = new ArrayList<>();
        officers.add(new Officer("OFF-101", "Insp. Vikram Rathore", "B-8801", "Inspector", "Digital Forensics & Reverse Engineering"));
        officers.add(new Officer("OFF-102", "SI Sneha Kulkarni", "B-9142", "Sub-Inspector", "Financial Cybercrime & UPI Tracing"));
        officers.add(new Officer("OFF-103", "Insp. Arjun Mehta", "B-7623", "Inspector", "Network Intrusion & Cyber Terrorism"));
        return officers;
    }

    public static SuspectNetworkGraph createSampleNetworkGraph() {
        SuspectNetworkGraph graph = new SuspectNetworkGraph();

        // Register Vertices
        EvidenceEntity s1 = new EvidenceEntity("SUSPECT-01", "Vikram Choudhary @ ShadowV", EntityType.SUSPECT, "Mule Recruiter & Phisher");
        EvidenceEntity s2 = new EvidenceEntity("SUSPECT-02", "Sameer Khan @ ByteGhost", EntityType.SUSPECT, "Malware Author & Ransomware Operator");
        EvidenceEntity s3 = new EvidenceEntity("SUSPECT-03", "Anand Verma", EntityType.SUSPECT, "Social Media Extortionist");

        EvidenceEntity ip1 = new EvidenceEntity("IP-103.21.244.12", "Delhi Broadband VPN Proxy", EntityType.IP_ADDRESS, "ISP: Spectra Delhi");
        EvidenceEntity ip2 = new EvidenceEntity("IP-45.118.65.201", "Tor Exit Relay Node", EntityType.IP_ADDRESS, "Location: Netherlands Gateway");

        EvidenceEntity dev1 = new EvidenceEntity("IMEI-863920194829102", "OnePlus 11 5G", EntityType.DEVICE_IMEI, "Primary Sim: Airtel");
        EvidenceEntity dev2 = new EvidenceEntity("DEVICE-SERVER-BGLR", "Dell PowerEdge R740", EntityType.DEVICE_IMEI, "Compromised Hospital Server");

        EvidenceEntity bank1 = new EvidenceEntity("BANK-HDFC-9921", "HDFC Lajpat Nagar AC-9921", EntityType.BANK_ACCOUNT, "Mule Account in Delhi");
        EvidenceEntity bank2 = new EvidenceEntity("BANK-ICICI-4412", "ICICI Indiranagar AC-4412", EntityType.BANK_ACCOUNT, "Shell Trade Account");

        EvidenceEntity ph1 = new EvidenceEntity("PHONE-+919876543210", "Airtel Delhi SIM", EntityType.PHONE_NUMBER, "Burner SIM card");
        EvidenceEntity ph2 = new EvidenceEntity("PHONE-+918765432109", "Jio Mumbai SIM", EntityType.PHONE_NUMBER, "WhatsApp caller ID");

        for (EvidenceEntity e : List.of(s1, s2, s3, ip1, ip2, dev1, dev2, bank1, bank2, ph1, ph2)) {
            graph.addVertex(e);
        }

        // Add Co-occurrence Edges
        // Case 1 (UPI Scam)
        graph.addEdge("SUSPECT-01", "IP-103.21.244.12", "USED_IP", "CCRRS-1001");
        graph.addEdge("SUSPECT-01", "BANK-HDFC-9921", "MULE_RECEIVER", "CCRRS-1001");

        // Case 4 (SIM Swap) - shares IP-103.21.244.12 and BANK-HDFC-9921!
        graph.addEdge("IMEI-863920194829102", "IP-103.21.244.12", "DEVICE_LOGGED_IP", "CCRRS-1004");
        graph.addEdge("IMEI-863920194829102", "BANK-HDFC-9921", "TRANSACTION_TERMINAL", "CCRRS-1004");

        // Case 2 (Ransomware)
        graph.addEdge("SUSPECT-02", "IP-45.118.65.201", "DEPLOYED_EXPLOIT_VIA", "CCRRS-1002");
        graph.addEdge("IP-45.118.65.201", "DEVICE-SERVER-BGLR", "INFILTRATED_TARGET", "CCRRS-1002");

        // Case 5 (Crypto Telegram Scam) - connects SUSPECT-02 with BANK-ICICI-4412 and PHONE
        graph.addEdge("SUSPECT-02", "BANK-ICICI-4412", "RECEIVES_PROCEEDS", "CCRRS-1005");
        graph.addEdge("BANK-ICICI-4412", "PHONE-+919876543210", "OTP_REGISTERED_PHONE", "CCRRS-1005");

        // Case 3 (Blackmail)
        graph.addEdge("SUSPECT-03", "PHONE-+919876543210", "CALLER_ORIGIN", "CCRRS-1003");

        // Case 6 (Gift card theft)
        graph.addEdge("SUSPECT-01", "PHONE-+918765432109", "DISPATCH_SMS_GATEWAY", "CCRRS-1006");

        return graph;
    }

    public static List<SuspectCandidate> createSuspectPoolForBacktracking() {
        List<SuspectCandidate> pool = new ArrayList<>();
        pool.add(new SuspectCandidate("SUS-101", "Vikram Choudhary", "103.21.244.12", "Android OnePlus", "UPI_SPOOFING", "Delhi", 88));
        pool.add(new SuspectCandidate("SUS-102", "Sameer Khan", "45.118.65.201", "Linux Server", "RANSOMWARE", "Bengaluru", 95));
        pool.add(new SuspectCandidate("SUS-103", "Anand Verma", "103.21.110.45", "Android Samsung", "EXTORTION", "Mumbai", 72));
        pool.add(new SuspectCandidate("SUS-104", "Manish Tiwari", "103.21.244.99", "Android Redmi", "UPI_SPOOFING", "Delhi", 79));
        pool.add(new SuspectCandidate("SUS-105", "Kunal Singhania", "182.72.14.3", "iOS iPhone", "IDENTITY_THEFT", "Kolkata", 65));
        pool.add(new SuspectCandidate("SUS-106", "Deepak Sharma", "103.21.244.50", "Android Vivo", "UPI_SPOOFING", "Delhi", 84));
        pool.add(new SuspectCandidate("SUS-107", "Ritu Malhotra", "103.21.80.12", "Windows Laptop", "PHISHING", "Gurugram", 68));
        return pool;
    }

    public static void populateSystem(SinglyLinkedList list, ComplaintBST bst, CategoryBinaryTree categoryTree) {
        List<Complaint> complaints = createSampleComplaints();
        for (Complaint c : complaints) {
            list.insertTail(c);
            bst.insert(c);
            categoryTree.recordComplaint(c.getCategory(), c.getFinancialLoss());
        }
        categoryTree.aggregateBottomUp(categoryTree.getRoot());
    }
}
