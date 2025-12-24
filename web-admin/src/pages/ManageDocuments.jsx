import React, { useEffect, useState } from "react";
import { collection, getDocs, deleteDoc, doc } from "firebase/firestore";
import { ref, deleteObject } from "firebase/storage";
import { db } from "../firebase";
import { storage } from "../firebase";


function ManageDocuments() {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchDocuments = async () => {
    try {
      const snapshot = await getDocs(collection(db, "DocumentID"));
      const list = snapshot.docs.map(docSnap => ({
        id: docSnap.id,
        ...docSnap.data()
      }));
      setDocuments(list);
    } catch (err) {
      alert("Lỗi tải dữ liệu!");
    } finally {
      setLoading(false);
    }
  };


  useEffect(() => {
    fetchDocuments();
  }, []);

  const deleteDocument = async (docId, fileUrl) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa bài đăng này?")) return;

    try {
      // 1️⃣ Xóa file trên Storage
      if (fileUrl) {
        const fileRef = ref(storage, fileUrl);
        await deleteObject(fileRef);
      }

      // 2️⃣ Xóa Firestore
      await deleteDoc(doc(db, "DocumentID", docId));

      alert("🗑️ Đã xóa bài đăng!");
      fetchDocuments();

    } catch (error) {
      alert("Lỗi xóa: " + error.message);
    }
  };

  if (loading) return <p>⏳ Đang tải bài đăng...</p>;

  return (
    <div>
      <h2>📂 Quản lý bài đăng</h2>

      {documents.length === 0 ? (
        <p>Không có bài đăng</p>
      ) : (
        <table border="1" cellPadding="10" width="100%">
          <thead>
            <tr>
              <th>Tiêu đề</th>
              <th>Môn học</th>
              <th>Người đăng</th>
              <th>File</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {documents.map(item => (
              <tr key={item.id}>
                <td>{item.title}</td>
                <td>{item.subject}</td>
                <td>{item.uploaderName || "Ẩn danh"}</td>
                <td>
                  <a href={item.fileUrl} target="_blank" rel="noreferrer">
                    Xem file
                  </a>
                </td>
                <td>
                  <button
                    style={{ background: "red", color: "#fff" }}
                    onClick={() => deleteDocument(item.id, item.fileUrl)}
                  >
                    Xóa
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default ManageDocuments;
