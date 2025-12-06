Staff Scheduling Android App – Edutech
A modern and efficient Android application designed to streamline **staff scheduling**, improve communication, and simplify workflow management for educational institutions.  
Built using **Java**, **XML**, and **Firebase**, the app delivers real-time updates, secure user authentication, and push notifications — all wrapped in a clean and intuitive UI.
📌 Key Features
👥 User Authentication
- Login & Signup using Firebase Authentication  
- Role-based access (Admin & Staff)

### 📅 Staff Scheduling
- Admin can assign duties, shifts, and tasks  
- Staff can view their assigned schedules  
- Real-time schedule updates via Firebase

### 🔔 Push Notifications (FCM)
- Individual staff notifications using **FCM tokens**  
- Admin can send custom alerts  

### 🧾 Data Management
- All user & schedule data stored in Firebase Realtime Database  
- Firebase Storage for uploading staff documents/images (if required)

### 💡 Modern Design
- Clean UI built with XML  
- Smooth navigation using Activities & ViewModels

---

## 🛠️ Tech Stack

| Component                  | Technology Used                         |
|----------------------------|------------------------------------------|
| Frontend                   | Java, XML                                |
| Backend (Cloud)            | Firebase Realtime Database               |
| Authentication             | Firebase Auth                            |
| Notifications              | Firebase Cloud Messaging (FCM)           |
| Storage                    | Firebase Storage                         |
| IDE                        | Android Studio                           |

---

## 📁 Project Structure
├── java/com/example/edutech/
│ ├── activities/ # Screens / UI logic
│ ├── utils/ # Helpers & notification utilities
│ ├── MyFirebaseMessagingService.java
│ └── authViewModel.java # Auth logic
├── res/
│ ├── layout/ # XML layouts
│ ├── drawable/ # Icons & shapes
│ └── values/ # Colors, strings, styles
└── AndroidManifest.xml

