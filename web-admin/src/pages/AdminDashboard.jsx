import React, { useState, useEffect } from "react";
import { FileText, Users, BarChart3, LogOut, Menu, X, Trash2 } from "lucide-react";
// Import auth và db từ file firebase config của bạn
import { auth, db } from "../firebase";
import { signOut } from "firebase/auth";
import { 
  collection, onSnapshot, doc, addDoc, 
  updateDoc, deleteDoc, query, limit 
} from "firebase/firestore";
import { 
  AreaChart, Area, XAxis, YAxis, CartesianGrid, 
  Tooltip, ResponsiveContainer, PieChart, Pie, Cell 
} from 'recharts';

function AdminDashboard() {
  const [tab, setTab] = useState("stats");
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const logout = async () => {
    if (window.confirm("Bạn có muốn đăng xuất không?")) {
      await signOut(auth);
      alert("Đăng xuất thành công!");
    }
  };

  const menuItems = [
    { id: "stats", icon: BarChart3, label: "Thống kê" },
    { id: "documents", icon: FileText, label: "Quản lý tài liệu" },
    { id: "users", icon: Users, label: "Quản lý người dùng" }
  ];
  

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar - Giữ nguyên giao diện của bạn */}
      <aside className={`${sidebarOpen ? "w-64" : "w-20"} bg-white border-r border-gray-200 transition-all duration-300 flex flex-col`}>
        <div className="h-16 flex items-center justify-between px-4 border-b border-gray-200">
          {sidebarOpen && (
            <div className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-lg flex items-center justify-center">
                <FileText className="w-5 h-5 text-white" />
              </div>
              <span className="font-semibold text-gray-800">Share Document </span>
            </div>
          )}
          <button onClick={() => setSidebarOpen(!sidebarOpen)} className="p-2 hover:bg-gray-100 rounded-lg">
            {sidebarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-1">
          {menuItems.map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.id}
                onClick={() => setTab(item.id)}
                className={`w-full flex items-center space-x-3 px-3 py-3 rounded-lg transition-all ${
                  tab === item.id ? "bg-gradient-to-r from-blue-500 to-cyan-500 text-white shadow-md" : "text-gray-700 hover:bg-gray-100"
                }`}
              >
                <Icon className="w-5 h-5 flex-shrink-0" />
                {sidebarOpen && <span className="font-medium">{item.label}</span>}
              </button>
            );
          })}
        </nav>

        <div className="px-3 py-4 border-t border-gray-200">
          <button onClick={logout} className="w-full flex items-center space-x-3 px-3 py-3 rounded-lg text-red-600 hover:bg-red-50">
            <LogOut className="w-5 h-5 flex-shrink-0" />
            {sidebarOpen && <span className="font-medium">Đăng xuất</span>}
          </button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-8">
          <h1 className="text-2xl font-bold text-gray-800">
            {menuItems.find((item) => item.id === tab)?.label || "Dashboard"}
          </h1>
          <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center text-white">A</div>
        </header>

        <main className="flex-1 overflow-y-auto p-8">
          {tab === "stats" && <StatsContent />}
          {tab === "documents" && <DocumentsContent />}
          {tab === "users" && <UsersContent />}
        </main>
      </div>
    </div>
  );
}

// --- COMPONENT THỐNG KÊ THẬT ---
function StatsContent() {
  const [statsData, setStatsData] = useState({
    docs: 0, 
    users: 0, 
    totalDownloads: 0, 
    totalLikes: 0, 
    majorData: []
  });

  useEffect(() => {
    // 1. Lấy dữ liệu tài liệu và tính toán cơ cấu chuyên ngành
    const unsubDocs = onSnapshot(collection(db, "DocumentID"), (snap) => {
      let downloads = 0;
      let likes = 0;
      let majorCounts = {}; 
      
      snap.forEach((doc) => {
        const data = doc.data();
        downloads += parseInt(data.downloads || 0);
        likes += parseInt(data.likes || 0);
        
        // Đếm số lượng theo chuyên ngành (major)
        const major = data.major || "Khác";
        majorCounts[major] = (majorCounts[major] || 0) + 1;
      });

      // Chuyển dữ liệu sang định dạng cho biểu đồ tròn
      const formattedMajorData = Object.keys(majorCounts).map(key => ({
        name: key, value: majorCounts[key]
      }));

      setStatsData(prev => ({
        ...prev,
        docs: snap.size,
        totalDownloads: downloads,
        totalLikes: likes,
        majorData: formattedMajorData
      }));
    });

    // 2. Lấy tổng số người dùng
    const unsubUsers = onSnapshot(collection(db, "users"), (snap) => {
      setStatsData(prev => ({ ...prev, users: snap.size }));
    });

    return () => { unsubDocs(); unsubUsers(); };
  }, []);

  const COLORS = ['#3b82f6', '#06b6d4', '#8b5cf6', '#ec4899', '#f59e0b'];

  return (
    <div className="space-y-8">
      {/* Hàng các thẻ số liệu */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {[
          { label: "Tổng tài liệu", value: statsData.docs, color: "blue" },
          { label: "Người dùng", value: statsData.users, color: "cyan" },
          { label: "Lượt thích", value: statsData.totalLikes, color: "blue" },
          { label: "Lượt tải xuống", value: statsData.totalDownloads, color: "cyan" }
        ].map((stat, i) => (
          <div key={i} className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium text-gray-500">{stat.label}</span>
              <span className="text-[10px] bg-blue-50 text-blue-600 px-2 py-0.5 rounded-full font-bold">LIVE</span>
            </div>
            <div className="text-3xl font-bold text-gray-800">{stat.value.toLocaleString()}</div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Biểu đồ xu hướng hoạt động */}
        <div className="lg:col-span-2 bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
          <h3 className="text-lg font-bold text-gray-800 mb-6">Xu hướng hoạt động</h3>
          <div className="h-80 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={[{name: 'T10', d: 5}, {name: 'T11', d: 12}, {name: 'T12', d: statsData.docs}]}>
                <defs>
                  <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f0f0f0" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#9ca3af', fontSize: 12}} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#9ca3af', fontSize: 12}} />
                <Tooltip />
                <Area type="monotone" dataKey="d" stroke="#3b82f6" fillOpacity={1} fill="url(#colorValue)" strokeWidth={3} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Biểu đồ tròn cơ cấu chuyên ngành */}
        <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
          <h3 className="text-lg font-bold text-gray-800 mb-6">Cơ cấu chuyên ngành</h3>
          <div className="h-80 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie 
                  data={statsData.majorData} 
                  innerRadius={70} 
                  outerRadius={100} 
                  paddingAngle={5} 
                  dataKey="value"
                >
                  {statsData.majorData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="mt-4 grid grid-cols-2 gap-2">
            {statsData.majorData.map((entry, index) => (
              <div key={index} className="flex items-center text-xs text-gray-600">
                <span className="w-2.5 h-2.5 rounded-full mr-2" style={{backgroundColor: COLORS[index % COLORS.length]}}></span>
                {entry.name}: {entry.value}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

// --- COMPONENT QUẢN LÝ TÀI LIỆU THẬT ---
function DocumentsContent() {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Truy vấn collection "DocumentID" từ Firestore của bạn
    const unsubscribe = onSnapshot(collection(db, "DocumentID"), (snapshot) => {
      const docsData = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      setDocuments(docsData);
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const deleteDocument = async (id) => {
    if (window.confirm("Bạn có chắc chắn muốn xóa tài liệu này?")) {
      try {
        await deleteDoc(doc(db, "DocumentID", id));
      } catch (error) {
        alert("Lỗi khi xóa: " + error.message);
      }
    }
  };

  if (loading) return <div>Đang tải dữ liệu...</div>;

  return (
    <div className="bg-white rounded-xl shadow-sm border overflow-hidden">
      <table className="w-full text-left">
        <thead className="bg-gray-50 border-b">
          <tr>
            <th className="px-6 py-3 text-xs font-semibold text-gray-600 uppercase">Tiêu đề</th>
            <th className="px-6 py-3 text-xs font-semibold text-gray-600 uppercase">Tác giả</th>
            <th className="px-6 py-3 text-xs font-semibold text-gray-600 uppercase">Môn học</th>
            <th className="px-6 py-3 text-xs font-semibold text-gray-600 uppercase">Định dạng</th>
            <th className="px-6 py-3 text-xs font-semibold text-gray-600 uppercase">Hành động</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200">
          {documents.map((doc) => (
            <tr key={doc.id} className="hover:bg-gray-50 transition-colors">
              <td className="px-6 py-4 text-sm font-medium text-gray-800 max-w-xs truncate">
                {doc.title || "Không có tiêu đề"}
              </td>
              <td className="px-6 py-4 text-sm text-gray-600">{doc.authorName || "Ẩn danh"}</td>
              <td className="px-6 py-4 text-sm text-gray-600">{doc.subject || "N/A"}</td>
              <td className="px-6 py-4 text-sm">
                <span className="px-2 py-1 bg-blue-50 text-blue-600 rounded text-xs font-bold">
                  {doc.docType || "PDF"}
                </span>
              </td>
              <td className="px-6 py-4">
                <button 
                  onClick={() => deleteDocument(doc.id)} 
                  className="text-red-600 hover:text-red-800 transition-colors"
                >
                  <Trash2 className="w-5 h-5" />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}


// --- COMPONENT QUẢN LÝ NGƯỜI DÙNG ---
function UsersContent() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);

  
  const toggleUserStatus = async (user) => {
    try {
      const userRef = doc(db, "users", user.id);
      // Nếu user.isActive chưa có thì mặc định là true, sau đó đảo ngược thành false
      const currentStatus = user.isActive ?? true;
      const newStatus = !currentStatus; 
      
      await updateDoc(userRef, { isActive: newStatus });
    } catch (error) {
      alert("Lỗi cập nhật: " + error.message);
    }
  };
  
  // State cho Form
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    className: "",
    department: "",
    isActive: true
  });

  // 1. Lấy dữ liệu Real-time
  useEffect(() => {
    const unsubscribe = onSnapshot(collection(db, "users"), (snapshot) => {
      setUsers(snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  // 2. Hàm Thêm hoặc Cập nhật
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingUser) {
        // Cập nhật người dùng
        const userRef = doc(db, "users", editingUser.id);
        await updateDoc(userRef, formData);
        alert("Cập nhật tài khoản thành công!");
      } else {
        // Thêm người dùng mới
        await addDoc(collection(db, "users"), {
          ...formData,
          courseYear: "2023-2024" // Mặc định hoặc lấy từ input
        });
        alert("Thêm tài khoản thành công!");
      }
      closeModal();
    } catch (error) {
      alert("Lỗi: " + error.message);
    }
  };

  // 3. Hàm Xóa
  const handleDelete = async (id) => {
    if (window.confirm("Bạn có chắc chắn muốn xóa tài khoản này?")) {
      await deleteDoc(doc(db, "users", id));
    }
  };

  const openEditModal = (user) => {
    setEditingUser(user);
    setFormData({
      fullName: user.fullName || "",
      email: user.email || "",
      className: user.className || "",
      department: user.department || "",
      isActive: user.isActive ?? true
    });
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setEditingUser(null);
    setFormData({ fullName: "", email: "", className: "", department: "", isActive: true });
  };

  if (loading) return <div className="p-8">Đang tải danh sách tài khoản...</div>;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-bold text-gray-800">Danh sách tài khoản</h2>
        <button 
          onClick={() => setModalOpen(true)}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          + Thêm tài khoản
        </button>
      </div>

     <div className="bg-white rounded-xl shadow-sm border overflow-x-auto">
  <table className="w-full text-left border-collapse">
    <thead className="bg-gray-50 border-b border-gray-200">
      <tr>
        <th className="px-6 py-4 text-xs font-semibold uppercase text-gray-600 w-1/4">Họ Tên</th>
        <th className="px-6 py-4 text-xs font-semibold uppercase text-gray-600 w-1/4">Email</th>
        <th className="px-6 py-4 text-xs font-semibold uppercase text-gray-600 w-1/6">Lớp</th>
        <th className="px-6 py-4 text-xs font-semibold uppercase text-gray-600 w-1/6 text-center">Trạng thái</th>
        <th className="px-6 py-4 text-xs font-semibold uppercase text-gray-600 w-1/6 text-center">Hành động</th>
      </tr>
    </thead>
    <tbody className="divide-y divide-gray-100">
      {users.map((user) => (
        <tr key={user.id} className="hover:bg-gray-50 transition-colors">
          <td className="px-6 py-4 text-sm font-medium text-gray-800">
            {user.fullName || "N/A"}
          </td>
          <td className="px-6 py-4 text-sm text-gray-600">
            {user.email}
          </td>
          <td className="px-6 py-4 text-sm text-gray-600">
            {user.className || "N/A"}
          </td>
          <td className="px-6 py-4 text-center">
            <button
              onClick={() => toggleUserStatus(user)}
              className={`px-3 py-1 rounded-full text-[11px] font-bold transition-all ${
                (user.isActive ?? true)
                  ? 'bg-green-100 text-green-700 hover:bg-green-200'
                  : 'bg-red-100 text-red-700 hover:bg-red-200'
              }`}
            >
              {(user.isActive ?? true) ? "● Hoạt động" : "○ Đã khóa"}
            </button>
          </td>
          <td className="px-6 py-4 text-center">
            <div className="flex justify-center space-x-4">
              <button 
                onClick={() => openEditModal(user)} 
                className="text-blue-600 hover:text-blue-800 text-sm font-medium"
              >
                Sửa
              </button>
              <button 
                onClick={() => handleDelete(user.id)} 
                className="text-red-600 hover:text-red-800 text-sm font-medium"
              >
                Xóa
              </button>
            </div>
          </td>
        </tr>
      ))}
    </tbody>
  </table>
</div>

      {/* MODAL THÊM/SỬA */}
      {modalOpen && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-xl">
            <h3 className="text-lg font-bold mb-4">{editingUser ? "Chỉnh sửa tài khoản" : "Thêm tài khoản mới"}</h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <input 
                type="text" placeholder="Họ và tên" required className="w-full border p-2 rounded"
                value={formData.fullName} onChange={(e) => setFormData({...formData, fullName: e.target.value})}
              />
              <input 
                type="email" placeholder="Email" required className="w-full border p-2 rounded"
                value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})}
              />
              <input 
                type="text" placeholder="Lớp (ví dụ: 23)" className="w-full border p-2 rounded"
                value={formData.className} onChange={(e) => setFormData({...formData, className: e.target.value})}
              />
              <select 
                className="w-full border p-2 rounded"
                value={formData.isActive} onChange={(e) => setFormData({...formData, isActive: e.target.value === "true"})}
              >
                <option value="true">Hoạt động</option>
                <option value="false">Khóa</option>
              </select>
              <div className="flex justify-end space-x-2 pt-4">
                <button type="button" onClick={closeModal} className="px-4 py-2 bg-gray-100 rounded">Hủy</button>
                <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded">Lưu</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminDashboard;