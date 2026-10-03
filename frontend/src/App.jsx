import { useState } from 'react'
import html2pdf from 'html2pdf.js'
import './App.css'

function App() {
  const [activeTab, setActiveTab] = useState('bmc') 
  const [contexto, setContexto] = useState('')
  const [parametros, setParametros] = useState('')
  
  const [jtbd, setJtbd] = useState(null)
  const [canvas, setCanvas] = useState(null)
  
  const [leanCanvas, setLeanCanvas] = useState(null)

  const [isLoading, setIsLoading] = useState(false)
  const [errorMsg, setErrorMsg] = useState(null)
  const [isExporting, setIsExporting] = useState(false)

  const handleGenerate = async () => {
    setIsLoading(true)
    setErrorMsg(null)
    
    if (activeTab === 'bmc') {
      setJtbd(null)
      setCanvas(null)
      try {
        const response = await fetch('http://localhost:8080/api/generate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ context: contexto, parameters: parametros }),
        })
        if (!response.ok) throw new Error('Servidor ocupado o caído.')
        const data = await response.json()
        if (!data.jtbd_analysis || !data.business_model_canvas) throw new Error('Formato inválido.')
        setJtbd(data.jtbd_analysis)
        setCanvas(data.business_model_canvas)
      } catch (error) {
        setErrorMsg("Error al generar BMC. Intenta de nuevo.")
      } finally {
        setIsLoading(false)
      }
    } else {
      setLeanCanvas(null)
      try {
        const response = await fetch('http://localhost:8080/api/generate-lean', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ context: contexto, parameters: parametros }),
        })
        if (!response.ok) throw new Error('Servidor ocupado o caído.')
        const data = await response.json()
        if (!data.lean_canvas) throw new Error('Formato inválido.')
        setLeanCanvas(data.lean_canvas)
      } catch (error) {
        setErrorMsg("Error al generar Lean Canvas. Intenta de nuevo.")
      } finally {
        setIsLoading(false)
      }
    }
  }

  const handleDownloadPDF = () => {
    setIsExporting(true)
    const elementId = activeTab === 'bmc' ? 'reporte-bmc' : 'reporte-lean'
    const element = document.getElementById(elementId)
    
    const opt = {
      margin: 0.5,
      filename: activeTab === 'bmc' ? 'Business_Model_Canvas.pdf' : 'Lean_Canvas.pdf',
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: { scale: 2, backgroundColor: '#0f172a' },
      jsPDF: { unit: 'in', format: 'a3', orientation: 'landscape' }
    }
    
    html2pdf().set(opt).from(element).save().then(() => setIsExporting(false))
  }

  const renderList = (items) => {
    if (!items || items.length === 0) return <li>No hay datos</li>
    return items.map((item, i) => <li key={i}>{item}</li>)
  }

  return (
    <div className="app-layout">
      {/* PANEL IZQUIERDO */}
      <div className="panel-formulario">
        <h2>Generadores AI</h2>
        
        {/* PESTAÑAS DE NAVEGACIÓN */}
        <div className="tabs-container">
          <button 
            className={`tab-button ${activeTab === 'bmc' ? 'active' : ''}`}
            onClick={() => setActiveTab('bmc')}
          >
            BMC & JTBD
          </button>
          <button 
            className={`tab-button ${activeTab === 'lean' ? 'active' : ''}`}
            onClick={() => setActiveTab('lean')}
          >
            Lean Canvas
          </button>
        </div>
        
        <label>
          {activeTab === 'bmc' ? 'Contexto (Idea o Problema):' : 'Problema central y solución inicial:'}
        </label>
        <textarea 
          rows="5" 
          value={contexto} 
          onChange={(e) => setContexto(e.target.value)} 
          placeholder={activeTab === 'bmc' 
            ? "Ej: App para conectar estudiantes que necesitan tutorías para parciales de la UCB..."
            : "Ej: Los estudiantes no tienen tiempo para almorzar entre clases. Solución: un sistema de viandas ágil..."}
        />

        <label>
          {activeTab === 'bmc' ? 'Parámetros Adicionales:' : 'Restricciones de mercado o ventajas técnicas:'}
        </label>
        <textarea 
          rows="3" 
          value={parametros} 
          onChange={(e) => setParametros(e.target.value)} 
          placeholder={activeTab === 'bmc'
            ? "Ej: Presupuesto limitado, uso de códigos QR..."
            : "Ej: Distribución a pie dentro del campus, pagos exclusivos por QR, solo comida saludable..."}
        />

        <button onClick={handleGenerate} disabled={isLoading || !contexto}>
          {isLoading ? 'Generando...' : `Generar ${activeTab === 'bmc' ? 'BMC' : 'Lean Canvas'}`}
        </button>

        {(canvas || leanCanvas) && (
          <button 
            onClick={handleDownloadPDF} 
            disabled={isExporting}
            style={{background: isExporting ? '#475569' : '#10b981', marginTop: '15px'}}
          >
            {isExporting ? '⏳ Procesando PDF...' : '⬇️ Descargar PDF'}
          </button>
        )}
      </div>

      {/* PANEL DERECHO */}
      <div className="panel-resultados">
        {errorMsg && <div className="error-banner"><strong>⚠️ Atención:</strong> {errorMsg}</div>}

        {!jtbd && !leanCanvas && !isLoading && !errorMsg && (
          <div className="estado-vacio">
            <p>Ingresa los datos y presiona "Generar" para comenzar.</p>
          </div>
        )}
        
        {isLoading && <p className="loading-text">Procesando análisis con Gemini y el sistema RAG...</p>}
        
        {/* VISTA 1: BUSINESS MODEL CANVAS */}
        {activeTab === 'bmc' && jtbd && canvas && !isLoading && (
          <div id="reporte-bmc" className="resultados-contenedor">
            <div className="jtbd-section">
              <h3>Análisis Jobs To Be Done</h3>
              <ul>
                <li><strong>Job Principal:</strong> {jtbd.job_principal}</li>
                <li><strong>Job Funcional:</strong> {jtbd.job_funcional}</li>
                <li><strong>Job Social:</strong> {jtbd.job_social}</li>
                <li><strong>Job Emocional:</strong> {jtbd.job_emocional}</li>
              </ul>
            </div>
            <h3>Business Model Canvas</h3>
            <div className="canvas-grid-scroll">
              <div className="canvas-grid">
                 <div className="bloque alianzas"><h4>Alianzas Clave</h4><ul>{renderList(canvas.alianzas_clave)}</ul></div>
                 <div className="bloque actividades"><h4>Actividades Clave</h4><ul>{renderList(canvas.actividades_clave)}</ul></div>
                 <div className="bloque recursos"><h4>Recursos Clave</h4><ul>{renderList(canvas.recursos_clave)}</ul></div>
                 <div className="bloque propuesta"><h4>Propuesta de Valor</h4><ul>{renderList(canvas.propuesta_valor)}</ul></div>
                 <div className="bloque relaciones"><h4>Relación con Clientes</h4><ul>{renderList(canvas.relacion_clientes)}</ul></div>
                 <div className="bloque canales"><h4>Canales</h4><ul>{renderList(canvas.canales)}</ul></div>
                 <div className="bloque segmentos"><h4>Segmentos de Clientes</h4><ul>{renderList(canvas.segmentos_clientes)}</ul></div>
                 <div className="bloque costos"><h4>Estructura de Costos</h4><ul>{renderList(canvas.estructura_costos)}</ul></div>
                 <div className="bloque ingresos"><h4>Fuentes de Ingresos</h4><ul>{renderList(canvas.fuentes_ingresos)}</ul></div>
              </div>
            </div>
          </div>
        )}

        {/* VISTA 2: LEAN CANVAS */}
        {activeTab === 'lean' && leanCanvas && !isLoading && (
          <div id="reporte-lean" className="resultados-contenedor">
            <h3>Lean Canvas</h3>
            <div className="canvas-grid-scroll">
              
              {/* Sección Superior (5 Columnas) */}
              <div className="lean-canvas-top">
                <div className="lean-col border-red">
                  <div className="bloque lean-sub-block"><h4>Problema</h4><ul>{renderList(leanCanvas.problemas)}</ul></div>
                  <div className="bloque lean-sub-block mt-15"><h4>Alternativas Existentes</h4><ul>{renderList(leanCanvas.alternativas_existentes)}</ul></div>
                </div>
                
                <div className="lean-col border-orange">
                  <div className="bloque lean-sub-block"><h4>Solución</h4><ul>{renderList(leanCanvas.solucion)}</ul></div>
                  <div className="bloque lean-sub-block mt-15"><h4>Métricas Clave</h4><ul>{renderList(leanCanvas.metricas_clave)}</ul></div>
                </div>
                
                <div className="lean-col border-indigo">
                  <div className="bloque lean-sub-block"><h4>Propuesta de Valor Única</h4><ul>{renderList(leanCanvas.propuesta_valor_unica)}</ul></div>
                  <div className="bloque lean-sub-block mt-15"><h4>Concepto de Alto Nivel</h4><ul>{renderList(leanCanvas.concepto_alto_nivel)}</ul></div>
                </div>
                
                <div className="lean-col border-green">
                  <div className="bloque lean-sub-block"><h4>Ventaja Injusta</h4><ul>{renderList(leanCanvas.ventaja_injusta)}</ul></div>
                  <div className="bloque lean-sub-block mt-15"><h4>Canales</h4><ul>{renderList(leanCanvas.canales)}</ul></div>
                </div>
                
                <div className="lean-col border-cyan">
                  <div className="bloque lean-sub-block"><h4>Segmentos de Clientes</h4><ul>{renderList(leanCanvas.segmentos_clientes)}</ul></div>
                  <div className="bloque lean-sub-block mt-15"><h4>Early Adopters</h4><ul>{renderList(leanCanvas.early_adopters)}</ul></div>
                </div>
              </div>

              {/* Sección Inferior (2 Columnas) */}
              <div className="lean-canvas-bottom">
                <div className="bloque border-slate">
                  <h4>Estructura de Costos</h4>
                  <ul>{renderList(leanCanvas.estructura_costos)}</ul>
                </div>
                <div className="bloque border-teal">
                  <h4>Fuentes de Ingresos</h4>
                  <ul>{renderList(leanCanvas.fuentes_ingresos)}</ul>
                </div>
              </div>

            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default App
