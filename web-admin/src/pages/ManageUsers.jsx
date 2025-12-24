import { collection, getDocs, updateDoc, doc } from "firebase/firestore";
import { db } from "../firebase";
import { useEffect, useState } from "react";

function ManageUsers() {
  const [users, setUsers] = useState([]);

  const loadUsers = async () => {
    const snap = await getDocs(collection(db, "users"));
    const list = snap.docs.map((d) => ({
      id: d.id,
      ...d.data(),
    }));
    setUsers(list);
  };

  const toggleUser = async (id, isActive) => {
    const newStatus = isActive === false ? true : false;

    await updateDoc(doc(db, "users", id), {
      isActive: newStatus,
    });

    loadUsers();
  };

  useEffect(() => {
    loadUsers();
  }, []);

  return (
    <div>
      <h2>👤 Quản lý tài khoản</h2>

      <table border="1" cellPadding="10">
        <thead>
          <tr>
            <th>Email</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>

        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.email}</td>
              <td style={{ color: u.isActive === false ? "red" : "green" }}>
                {u.isActive === false ? "Đã khóa" : "Hoạt động"}
              </td>
              <td>
                <button onClick={() => toggleUser(u.id, u.isActive)}>
                  {u.isActive === false ? "Mở" : "Khóa"}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default ManageUsers;
