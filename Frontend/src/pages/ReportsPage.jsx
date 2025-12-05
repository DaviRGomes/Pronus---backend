import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import html2pdf from 'html2pdf.js';

export default function ReportsPage() {
    const { user } = useAuth();
    const [generating, setGenerating] = useState(false);
    const [dashboardData, setDashboardData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchDashboardData();
    }, [user.id]);

    const fetchDashboardData = async () => {
        try {
            const response = await fetch(`http://localhost:8080/api/sessao-treino/dashboard/${user.id}`);
            if (response.ok) {
                const data = await response.json();
                setDashboardData(data);
            } else {
                console.error('Erro ao buscar dados do dashboard');
            }
        } catch (error) {
            console.error('Erro na requisição do dashboard:', error);
        } finally {
            setLoading(false);
        }
    };

    const generateReport = async () => {
        setGenerating(true);
        // Simula geração de relatório
        setTimeout(() => {
            const reportContent = document.getElementById('report-content');
            html2pdf().set({
                margin: 15,
                filename: `relatorio-${user.nome}-${new Date().toISOString().split('T')[0]}.pdf`,
                html2canvas: { scale: 2 },
                jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
            }).from(reportContent).save();
            setGenerating(false);
        }, 1000);
    };

    if (loading) {
        return <div style={{color: '#fff'}}>Carregando dados...</div>;
    }

    return (
        <div>
            <h1 style={{color:'#f8fafc',fontSize:28,marginBottom:24}}>📄 Relatórios</h1>

            <div className="card" style={{marginBottom:24}}>
                <h2 style={{color:'#f8fafc',fontSize:20,marginBottom:16}}>Gerar Relatório</h2>
                <p style={{color:'#94a3b8',marginBottom:20}}>Exporte um relatório completo do seu progresso em formato PDF.</p>
                <div style={{display:'flex',gap:12}}>
                    <button onClick={generateReport} className="btn btn-primary" disabled={generating}>
                        {generating ? '⏳ Gerando...' : '📄 Gerar Relatório Completo'}
                    </button>
                </div>
            </div>

            {/* Conteúdo do Relatório (escondido, usado para PDF) */}
            <div id="report-content" style={{background:'#fff',color:'#000',padding:40,borderRadius:16}}>
                <div style={{textAlign:'center',marginBottom:32,borderBottom:'2px solid #6366f1',paddingBottom:24}}>
                    <h1 style={{color:'#6366f1',fontSize:28,margin:0}}>🗣️ FonoFlow</h1>
                    <p style={{color:'#666',marginTop:8}}>Relatório de Progresso</p>
                </div>

                <div style={{marginBottom:24}}>
                    <h2 style={{color:'#333',fontSize:20,marginBottom:12}}>Dados do Paciente</h2>
                    <table style={{width:'100%'}}>
                        <tbody>
                            <tr><td style={{padding:8,color:'#666'}}>Nome:</td><td style={{padding:8,fontWeight:600}}>{user.nome}</td></tr>
                            <tr><td style={{padding:8,color:'#666'}}>ID:</td><td style={{padding:8}}>{user.id}</td></tr>
                            <tr><td style={{padding:8,color:'#666'}}>Data do Relatório:</td><td style={{padding:8}}>{new Date().toLocaleDateString('pt-BR')}</td></tr>
                        </tbody>
                    </table>
                </div>

                <div style={{marginBottom:24}}>
                    <h2 style={{color:'#333',fontSize:20,marginBottom:12}}>Resumo de Desempenho</h2>
                    <div style={{display:'grid',gridTemplateColumns:'repeat(3,1fr)',gap:16}}>
                        <div style={{background:'#f0f9ff',padding:16,borderRadius:12,textAlign:'center'}}>
                            <div style={{fontSize:28,fontWeight:700,color:'#6366f1'}}>
                                {dashboardData ? dashboardData.sessoesRealizadas : 0}
                            </div>
                            <div style={{color:'#666',fontSize:13}}>Sessões Realizadas</div>
                        </div>
                        <div style={{background:'#f0fdf4',padding:16,borderRadius:12,textAlign:'center'}}>
                            <div style={{fontSize:28,fontWeight:700,color:'#22c55e'}}>
                                {dashboardData ? dashboardData.pontuacaoMedia : 0}%
                            </div>
                            <div style={{color:'#666',fontSize:13}}>Pontuação Média</div>
                        </div>
                        <div style={{background:'#fefce8',padding:16,borderRadius:12,textAlign:'center'}}>
                            <div style={{fontSize:28,fontWeight:700,color: (dashboardData?.evolucao >= 0 ? '#eab308' : '#ef4444')}}>
                                {dashboardData && dashboardData.evolucao >= 0 ? '+' : ''}{dashboardData ? dashboardData.evolucao : 0}%
                            </div>
                            <div style={{color:'#666',fontSize:13}}>Evolução</div>
                        </div>
                    </div>
                </div>

                <div style={{marginBottom:24}}>
                    <h2 style={{color:'#333',fontSize:20,marginBottom:12}}>Observações</h2>
                    <p style={{color:'#666',lineHeight:1.8}}>
                        {dashboardData ? dashboardData.observacao : "Nenhuma observação disponível."}
                    </p>
                </div>

                <div style={{borderTop:'1px solid #eee',paddingTop:16,textAlign:'center',color:'#999',fontSize:12}}>
                    FonoFlow - Sistema de Treino de Pronúncia com IA<br/>
                    Relatório gerado automaticamente em {new Date().toLocaleString('pt-BR')}
                </div>
            </div>
        </div>
    );
}

