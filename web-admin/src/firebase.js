import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import { getStorage } from "firebase/storage";

const firebaseConfig = {
  apiKey: "AIzaSyBMy2vHoNwm931yxYwRUoXA4vH1Zs_rp0I",
  authDomain: "bcck-e7a7d.firebaseapp.com",
  projectId: "bcck-e7a7d",
  storageBucket: "bcck-e7a7d.firebasestorage.app",
  messagingSenderId: "1055755467522",
  appId: "1:1055755467522:web:20bca364f6b7e2edb3201b"
};

const app = initializeApp(firebaseConfig);

export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);

export default app;
