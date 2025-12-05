import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth, API } from '../contexts/AuthContext';

export default function DashboardLayout() {
    const { user, token, logout } = useAuth();
    const [especialistas, setEspecialistas] = useState([]);
    const [selectedEsp, setSelectedEsp] = useState(null);
    const [sessoes, setSessoes] = useState([]);
    const [loadingSessoes, setLoadingSessoes] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        fetchEspecialistas();
        fetchSessoes();
    }, []);

    const fetchEspecialistas = async () => {
        try {
            const res = await fetch(`${API}/especialistas`, { headers: { Authorization: `Bearer ${token}` }});
            const data = await res.json();
            setEspecialistas(data);
            if (data.length) setSelectedEsp(data[0]);
        } catch (e) { console.error(e); }
    };

    const fetchSessoes = async () => {
        setLoadingSessoes(true);
        try {
            const res = await fetch(`${API}/api/sessao-treino/historico/cliente/${user.id}`, { headers: { Authorization: `Bearer ${token}` }});
            if (res.ok) {
                const data = await res.json();
                setSessoes(data);
            }
        } catch (e) { console.error(e); }
        setLoadingSessoes(false);
    };

    const menuItems = [
        { path: '/', icon: '🏠', label: 'Início' },
        { path: '/session', icon: '🎤', label: 'Nova Sessão' },
        { path: '/history', icon: '📊', label: 'Histórico' },
        { path: '/reports', icon: '📄', label: 'Relatórios' },
        { path: '/profile', icon: '👤', label: 'Perfil' },
    ];

    return (
        <div style={{display:'flex',minHeight:'100vh'}}>
            {/* Sidebar */}
            <aside style={{width:260,background:'rgba(15,23,42,0.9)',padding:20,display:'flex',flexDirection:'column',borderRight:'1px solid rgba(255,255,255,0.1)'}}>
                <div style={{display:'flex',alignItems:'center',gap:12,marginBottom:32}}>
                    <span style={{fontSize:32}}>🗣️</span>
                    <span style={{color:'#f8fafc',fontSize:20,fontWeight:700}}>FonoFlow</span>
                </div>

                <nav style={{flex:1}}>
                    {menuItems.map(item => (
                        <div key={item.path} 
                             className={`nav-item ${location.pathname === item.path ? 'active' : ''}`} 
                             onClick={() => navigate(item.path)}>
                            <span style={{fontSize:20}}>{item.icon}</span>
                            <span>{item.label}</span>
                        </div>
                    ))}
                </nav>

                <div style={{borderTop:'1px solid rgba(255,255,255,0.1)',paddingTop:20}}>
                    <div style={{display:'flex',alignItems:'center',gap:12,marginBottom:16}}>
                        <div style={{width:40,height:40,borderRadius:'50%',background:'linear-gradient(135deg,#6366f1,#8b5cf6)',display:'flex',alignItems:'center',justifyContent:'center',color:'#fff',fontWeight:600}}>
                            {user?.nome.charAt(0)}
                        </div>
                        <div>
                            <div style={{color:'#f8fafc',fontSize:14,fontWeight:600}}>{user?.nome}</div>
                            <div style={{color:'#64748b',fontSize:12}}>{user?.tipo}</div>
                        </div>
                    </div>
                    <button onClick={logout} className="btn btn-secondary" style={{width:'100%'}}>
                        Sair
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main style={{flex:1,padding:32,overflow:'auto'}}>
                <Outlet context={{ sessoes, especialistas, selectedEsp, setSelectedEsp, fetchSessoes }} />
            </main>
        </div>
    );
}
