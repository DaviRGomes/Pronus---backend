import React, { useState, useRef, useEffect } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { useAuth, API } from '../contexts/AuthContext';
import html2pdf from 'html2pdf.js';

export default function SessionPage() {
    const { user, token } = useAuth();
    const { selectedEsp, fetchSessoes } = useOutletContext();
    const navigate = useNavigate();

    const [stage, setStage] = useState('setup'); // setup, session, result
    const [dificuldade, setDificuldade] = useState('R');
    const [sessaoId, setSessaoId] = useState(null);
    const [msgs, setMsgs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [recording, setRecording] = useState(false);
    const [audioBlob, setAudioBlob] = useState(null);
    const [resultado, setResultado] = useState(null);
    const mediaRef = useRef(null);
    const chunksRef = useRef([]);
    const endRef = useRef(null);

    const difs = [
        { v: 'R', l: 'R', d: 'rato, carro', icon: '🔴' },
        { v: 'L', l: 'L', d: 'lua, bola', icon: '🔵' },
        { v: 'S', l: 'S', d: 'sapo, massa', icon: '🟢' },
        { v: 'CH', l: 'CH', d: 'chuva, bicho', icon: '🟡' },
        { v: 'X', l: 'X', d: 'Xuxa, Sasha', icon: '🟣' },
        { v: 'LH', l: 'LH', d: 'palha, filho', icon: '🟠' },
    ];

    useEffect(() => { endRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [msgs]);

    const startSession = async () => {
        setLoading(true);
        try {
            const res = await fetch(`${API}/api/sessao-treino/iniciar`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
                body: JSON.stringify({ clienteId: user.id, especialistaId: selectedEsp?.id || 8, dificuldade, idade: 25 })
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.erro || 'Erro ao iniciar');
            if (Array.isArray(data)) {
                setMsgs(data.map(m => ({ id: Math.random(), type: 'bot', ...m, ts: new Date() })));
                if (data[0]?.sessaoId) setSessaoId(data[0].sessaoId);
            }
            setStage('session');
        } catch (e) {
            alert('Erro: ' + e.message);
        }
        setLoading(false);
    };

    const startRec = async () => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            mediaRef.current = new MediaRecorder(stream);
            chunksRef.current = [];
            mediaRef.current.ondataavailable = e => chunksRef.current.push(e.data);
            mediaRef.current.onstop = () => {
                setAudioBlob(new Blob(chunksRef.current, { type: 'audio/webm' }));
                stream.getTracks().forEach(t => t.stop());
            };
            mediaRef.current.start();
            setRecording(true);
        } catch (e) {
            alert('Erro ao acessar microfone: ' + e.message);
        }
    };

    const stopRec = () => { 
        if (mediaRef.current) { 
            mediaRef.current.stop(); 
            setRecording(false); 
        } 
    };

    const sendAudio = async () => {
        if (!audioBlob || !sessaoId) return;
        setLoading(true);
        setMsgs(p => [...p, { id: Math.random(), type: 'user', mensagem: '🎤 Áudio enviado', ts: new Date() }]);
        try {
            const fd = new FormData();
            fd.append('audio', audioBlob, 'audio.webm');
            const res = await fetch(`${API}/api/sessao-treino/${sessaoId}/audio?usarGemini=true`, {
                method: 'POST', headers: { Authorization: `Bearer ${token}` }, body: fd
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.erro || 'Erro');
            if (Array.isArray(data)) {
                setMsgs(p => [...p, ...data.map(m => ({ id: Math.random(), type: 'bot', ...m, ts: new Date() }))]);
                const finalMsg = data.find(m => m.sessaoFinalizada);
                if (finalMsg) {
                    setResultado(finalMsg);
                    setStage('result');
                }
            }
            setAudioBlob(null);
        } catch (e) {
            setMsgs(p => [...p, { id: Math.random(), type: 'error', mensagem: e.message, ts: new Date() }]);
        }
        setLoading(false);
    };

    const handleBack = () => {
        fetchSessoes();
        navigate('/');
    };

    // SETUP STAGE
    if (stage === 'setup') {
        return (
            <div>
                <button onClick={handleBack} className="btn btn-secondary" style={{marginBottom:24}}>← Voltar</button>
                <div className="card" style={{maxWidth:600,margin:'0 auto',textAlign:'center'}}>
                    <div style={{fontSize:64,marginBottom:16}}>🎯</div>
                    <h2 style={{color:'#f8fafc',fontSize:28,marginBottom:8}}>Configurar Sessão</h2>
                    <p style={{color:'#94a3b8',marginBottom:32}}>Escolha o fonema que deseja praticar:</p>
                    
                    <div style={{display:'grid',gridTemplateColumns:'repeat(3, 1fr)',gap:12,marginBottom:32}}>
                        {difs.map(d => (
                            <button key={d.v} onClick={() => setDificuldade(d.v)}
                                style={{padding:20,borderRadius:16,border: dificuldade === d.v ? '3px solid #6366f1' : '2px solid rgba(255,255,255,0.1)',
                                background: dificuldade === d.v ? 'rgba(99,102,241,0.2)' : 'rgba(15,23,42,0.6)',cursor:'pointer',transition:'all 0.2s'}}>
                                <div style={{fontSize:32,marginBottom:8}}>{d.icon}</div>
                                <div style={{color:'#f8fafc',fontWeight:700,fontSize:18}}>{d.l}</div>
                                <div style={{color:'#64748b',fontSize:12,marginTop:4}}>{d.d}</div>
                            </button>
                        ))}
                    </div>

                    <button onClick={startSession} className="btn btn-primary" disabled={loading} style={{padding:'16px 48px',fontSize:18}}>
                        {loading ? '⏳ Preparando...' : '🚀 Começar Treino'}
                    </button>
                </div>
            </div>
        );
    }

    // RESULT STAGE
    if (stage === 'result') {
        const resumo = resultado?.resumoSessao || {};
        return (
            <div>
                <div className="card" style={{maxWidth:600,margin:'0 auto',textAlign:'center'}} id="resultado-sessao">
                    <div style={{fontSize:64,marginBottom:16}}>🏆</div>
                    <h2 style={{color:'#f8fafc',fontSize:28,marginBottom:24}}>Sessão Finalizada!</h2>
                    
                    <div style={{display:'grid',gridTemplateColumns:'repeat(2, 1fr)',gap:20,marginBottom:32}}>
                        <div className="stat-card">
                            <div className="stat-value" style={{color:'#22c55e'}}>{resumo.totalAcertos || 0}/{resumo.totalPalavras || 0}</div>
                            <div className="stat-label">Acertos</div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-value" style={{color:'#6366f1'}}>{(resumo.pontuacaoGeral || 0).toFixed(0)}%</div>
                            <div className="stat-label">Pontuação</div>
                        </div>
                    </div>

                    {resumo.pontosFortes?.length > 0 && (
                        <div style={{textAlign:'left',marginBottom:16}}>
                            <h3 style={{color:'#22c55e',fontSize:16,marginBottom:8}}>💪 Pontos Fortes:</h3>
                            {resumo.pontosFortes.map((p, i) => (
                                <p key={i} style={{color:'#94a3b8',marginLeft:16}}>• {p}</p>
                            ))}
                        </div>
                    )}

                    {resumo.pontosAMelhorar?.length > 0 && (
                        <div style={{textAlign:'left',marginBottom:24}}>
                            <h3 style={{color:'#eab308',fontSize:16,marginBottom:8}}>📈 A Melhorar:</h3>
                            {resumo.pontosAMelhorar.map((p, i) => (
                                <p key={i} style={{color:'#94a3b8',marginLeft:16}}>• {p}</p>
                            ))}
                        </div>
                    )}

                    <div style={{display:'flex',gap:12,justifyContent:'center'}} className="no-print">
                        <button onClick={() => { setStage('setup'); setMsgs([]); setSessaoId(null); setResultado(null); }} className="btn btn-primary">
                            🔄 Nova Sessão
                        </button>
                        <button onClick={() => {
                            const element = document.getElementById('resultado-sessao');
                            html2pdf().set({ margin: 10, filename: `sessao-${Date.now()}.pdf` }).from(element).save();
                        }} className="btn btn-secondary">
                            📄 Exportar PDF
                        </button>
                        <button onClick={handleBack} className="btn btn-secondary">
                            🏠 Voltar
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    // SESSION STAGE
    return (
        <div style={{display:'flex',flexDirection:'column',height:'calc(100vh - 64px)'}}>
            <div style={{display:'flex',alignItems:'center',justifyContent:'space-between',marginBottom:16}} className="no-print">
                <button onClick={handleBack} className="btn btn-secondary">← Voltar</button>
                <div style={{display:'flex',alignItems:'center',gap:8}}>
                    <span style={{color:'#f8fafc',fontWeight:600}}>Fonema:</span>
                    <span className="badge badge-success" style={{fontSize:16,padding:'8px 16px'}}>{dificuldade}</span>
                </div>
            </div>

            {/* Messages */}
            <div className="card" style={{flex:1,overflow:'auto',marginBottom:16}}>
                <div style={{display:'flex',flexDirection:'column',gap:16}}>
                    {msgs.map(m => (
                        <div key={m.id} style={{display:'flex',justifyContent: m.type === 'bot' ? 'flex-start' : 'flex-end',alignItems:'flex-end',gap:8}}>
                            {m.type === 'bot' && <div style={{width:36,height:36,borderRadius:'50%',background:'linear-gradient(135deg,#6366f1,#8b5cf6)',display:'flex',alignItems:'center',justifyContent:'center',fontSize:18}}>🤖</div>}
                            <div style={{maxWidth:'75%',padding:16,borderRadius:16,
                                background: m.type === 'bot' ? 'rgba(15,23,42,0.8)' : m.type === 'error' ? 'rgba(239,68,68,0.2)' : 'linear-gradient(135deg,#6366f1,#8b5cf6)',
                                border: m.type === 'error' ? '1px solid rgba(239,68,68,0.3)' : 'none'}}>
                                <p style={{color:'#f8fafc',margin:0,lineHeight:1.6}}>{m.mensagem}</p>
                                {m.palavras?.map((p, i) => (
                                    <div key={i} style={{marginTop:12,padding:20,background:'rgba(99,102,241,0.15)',borderRadius:12,textAlign:'center',border:'2px dashed rgba(99,102,241,0.4)'}}>
                                        <span style={{color:'#e2e8f0',fontSize:20,fontWeight:600}}>📢 {p}</span>
                                    </div>
                                ))}
                                {m.analise && (
                                    <div style={{marginTop:16,background:'rgba(0,0,0,0.2)',borderRadius:12,padding:16}}>
                                        <div style={{display:'flex',justifyContent:'space-between',marginBottom:12}}>
                                            <span style={{color:'#a5b4fc',fontWeight:600}}>📊 Análise</span>
                                            <span style={{background:'#6366f1',padding:'4px 12px',borderRadius:20,color:'#fff',fontWeight:700}}>{(m.analise.pontuacaoGeral || 0).toFixed(0)}%</span>
                                        </div>
                                        {m.analise.resultados?.map((r, i) => (
                                            <div key={i} style={{display:'flex',alignItems:'center',gap:12,padding:'10px 0',borderBottom:'1px solid rgba(255,255,255,0.05)'}}>
                                                <span style={{color: r.acertou ? '#22c55e' : '#ef4444',fontSize:20,fontWeight:'bold'}}>{r.acertou ? '✓' : '✗'}</span>
                                                <span style={{color:'#f8fafc',fontWeight:500,flex:1}}>{r.palavraEsperada}</span>
                                                <span style={{color:'#94a3b8',fontSize:13}}>{r.feedback}</span>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                    {loading && <div style={{display:'flex',gap:6,padding:16}}><span className="pulse" style={{color:'#6366f1',fontSize:20}}>●</span><span className="pulse" style={{color:'#6366f1',fontSize:20,animationDelay:'0.2s'}}>●</span><span className="pulse" style={{color:'#6366f1',fontSize:20,animationDelay:'0.4s'}}>●</span></div>}
                    <div ref={endRef} />
                </div>
            </div>

            {/* Recording Controls */}
            <div className="card no-print" style={{display:'flex',alignItems:'center',justifyContent:'center',gap:16,padding:20}}>
                {audioBlob ? (
                    <>
                        <audio src={URL.createObjectURL(audioBlob)} controls style={{height:40,flex:1,maxWidth:300}} />
                        <button onClick={() => setAudioBlob(null)} className="btn btn-danger">❌ Cancelar</button>
                        <button onClick={sendAudio} className="btn btn-success" disabled={loading}>{loading ? '⏳' : '📤'} Enviar</button>
                    </>
                ) : (
                    <button onClick={recording ? stopRec : startRec} className={`btn ${recording ? 'btn-danger pulse' : 'btn-primary'}`} style={{padding:'16px 48px',fontSize:18}}>
                        <span style={{marginRight:12,fontSize:24}}>{recording ? '⏹️' : '🎤'}</span>
                        {recording ? 'Parar Gravação' : 'Gravar Áudio'}
                    </button>
                )}
            </div>
        </div>
    );
}
