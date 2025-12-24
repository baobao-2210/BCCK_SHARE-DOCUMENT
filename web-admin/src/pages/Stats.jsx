import React, { useEffect, useState } from "react";
import { collection, getDocs, query, orderBy, limit } from "firebase/firestore";
import { db } from "../firebase";
import { Chart as ChartJS, BarElement, ArcElement, CategoryScale, LinearScale, Tooltip, Legend } from "chart.js";
import { Bar, Pie } from "react-chartjs-2";

ChartJS.register(BarElement, ArcElement, CategoryScale, LinearScale, Tooltip, Legend);

function Stats() {
  const [topDownloads, setTopDownloads] = useState([]);
  const [topLikes, setTopLikes] = useState([]);
  const [totalDocs, setTotalDocs] = useState(0);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    // 1. Lấy Top 5 Tải về (Sửa từ downloadCount -> downloads)
    const qDown = query(collection(db, "DocumentID"), orderBy("downloads", "desc"), limit(5));
    const snapDown = await getDocs(qDown);
    setTopDownloads(snapDown.docs.map(d => ({ id: d.id, ...d.data() })));

    // 2. Lấy Top 5 Like (Sửa từ likeCount -> likes)
    const qLike = query(collection(db, "DocumentID"), orderBy("likes", "desc"), limit(5));
    const snapLike = await getDocs(qLike);
    setTopLikes(snapLike.docs.map(d => ({ id: d.id, ...d.data() })));

    // 3. Tính tổng số tài liệu
    const allDocs = await getDocs(collection(db, "DocumentID"));
    setTotalDocs(allDocs.size);
  };

  const chartOptions = {
    responsive: true,
    plugins: { legend: { position: 'bottom' } }
  };

  return (
    <div className="space-y-8">
      {/* Thẻ tổng quan nhanh */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-blue-500 text-white p-6 rounded-xl shadow-lg">
          <p className="text-sm opacity-80 uppercase font-bold">Tổng tài liệu</p>
          <h2 className="text-4xl font-bold">{totalDocs}</h2>
        </div>
        <div className="bg-emerald-500 text-white p-6 rounded-xl shadow-lg">
          <p className="text-sm opacity-80 uppercase font-bold">Lượt tải nhiều nhất</p>
          <h2 className="text-4xl font-bold">{topDownloads[0]?.downloads || 0}</h2>
        </div>
        <div className="bg-amber-500 text-white p-6 rounded-xl shadow-lg">
          <p className="text-sm opacity-80 uppercase font-bold">Top Major</p>
          <h2 className="text-4xl font-bold">CNTT</h2>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Biểu đồ Downloads */}
        <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
          <h3 className="text-lg font-bold mb-4 flex items-center gap-2">⬇️ Top 5 Tải về nhiều nhất</h3>
          <div className="h-64">
            <Bar 
              options={chartOptions}
              data={{
                labels: topDownloads.map(d => d.title?.substring(0, 15) + "..."),
                datasets: [{ label: 'Lượt tải', data: topDownloads.map(d => d.downloads), backgroundColor: '#3b82f6' }]
              }} 
            />
          </div>
        </div>

        {/* Biểu đồ Likes */}
        <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
          <h3 className="text-lg font-bold mb-4 flex items-center gap-2">❤️ Top 5 Yêu thích</h3>
          <div className="flex justify-center h-64">
            <Pie 
              data={{
                labels: topLikes.map(d => d.title?.substring(0, 15) + "..."),
                datasets: [{ data: topLikes.map(d => d.likes), backgroundColor: ['#ef4444', '#f97316', '#f59e0b', '#10b981', '#3b82f6'] }]
              }} 
            />
          </div>
        </div>
      </div>
    </div>
  );
}

export default Stats;