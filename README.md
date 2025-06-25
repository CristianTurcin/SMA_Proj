# FitTrack: Fitness & Nutrition Tracker 📊💪

This Android mobile application was developed as a Bachelor’s thesis project and aims to combine fitness and nutrition tracking with **machine learning** for a personalized progress monitoring experience.

Users can log their daily exercises, meals, and steps, with all data automatically saved in Firebase. Based on a 14-day history (weight, calories, and steps), the app uses a BiLSTM ML model to **predict body weight for the next 7 days**.

All data is displayed in an intuitive and interactive UI, including charts and a mini-dashboard in the user profile, which shows indicators like **BMI**, **BMR**, and **TDEE**. The app also detects weight stagnation periods (plateaus) and provides visual alerts.

Data is protected with strict Firebase security rules and is accessible only to the authenticated user. Full integration with an externally hosted Flask server allows the ML model to run without relying on on-device TensorFlow, making the app fast and efficient.

The ultimate goal is to help users better understand their body evolution and offer smart tools to support their fitness and nutrition goals.

---

## 🔍 Main Features

- 🏋️‍♂️ **Exercise Journal** – daily exercise log (type, sets, reps, weight)
- 🍽️ **Meal Tracker** – meal logging with calorie & macronutrient calculation
- 👣 **Step Counter** – real-time step tracking with daily saving
- 📈 **Weight Chart** – weight evolution graph
- 🤖 **ML Weight Prediction** – BiLSTM model trained on historical data to predict weight for the next 7 days
- 🔒 **Secure Firebase Integration** – user-specific access & strict security rules
- 🧮 **Nutrition Mini-dashboard** – shows BMI, BMR, and TDEE
- 🔐 **Firebase Authentication** – via email and password
- 📁 **Structured data saving** – under `users/<uid>/`, `meals/<uid>/`, `exercises/<uid>/`

---

## ⚙️ Technologies Used

📱 **Android App**
- Kotlin (Jetpack)
- Android Studio
- MPAndroidChart – for data visualization
- Retrofit2 – for HTTP requests to the ML API

☁️ **Backend / Cloud**
- Firebase Authentication
- Firebase Realtime Database
- Custom Firebase Security Rules

🧠 **Machine Learning**
- Python + TensorFlow (BiLSTM)
- Google Colab – for model training
- Flask – REST API server for predictions
- Railway – for Flask server hosting

---

## 🔮 Machine Learning (BiLSTM)

The model is trained in Google Colab using 14 days of historical data (weight, steps, calories) and returns predictions for the next 7 days. It is hosted on a Flask server and accessed from the app via Retrofit.

---

## 🛡️ Security

- All user data is stored under `users/<uid>/`, `meals/<uid>/`, and `exercises/<uid>/`
- Firebase security rules restrict access to authenticated users only
