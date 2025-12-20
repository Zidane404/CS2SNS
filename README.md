# 🔒 Secure Chat Application for (CS2SNS)

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![Security](https://img.shields.io/badge/Security-SSL%2FTLS-blue?style=for-the-badge)](https://en.wikipedia.org/wiki/Transport_Layer_Security)

A robust multi-threaded chat application built in Java that demonstrates secure socket communication using **SSL/TLS encryption**. This project allows multiple clients to communicate over a local network with full encryption, protecting the contents of messages from attackers using packet sniffing.

🔗 **Repo:** [https://github.com/Zidane404/CS2SNS](https://github.com/Zidane404/CS2SNS)

---

## ✨ Features

* **End-to-End Encryption:** All traffic between client and server is encrypted using TLS (Transport Layer Security).
* **Multi-threaded Server:** Handles multiple concurrent users using Java's `ExecutorService`.
* **Real-time Messaging:** Instant broadcast of messages to all connected clients.
* **Cross-Machine Support:** Designed to work across different computers on a LAN (requires firewall configuration).
* **Graceful Handling:** Manages user disconnects and connection errors cleanly.


Because this application uses SSL, you must generate security certificates (Keystore and Truststore) before running the code.
