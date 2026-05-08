# 🏥 Sistema de Triaje Hospitalario — Scala

**Entrega 3 · Algoritmos y Estructura de Datos**  
Universidad del Valle de Guatemala · Facultad de Ingeniería

| Integrante | Carné |
|---|---|
| Franco Paiz | 25780 |
| Junior Lancerio | 25789 |

---

## 📌 Descripción del Proyecto

Sistema de gestión de triaje hospitalario que demuestra el uso combinado de múltiples estructuras de datos en Scala. El sistema permite registrar pacientes, despachar al más urgente y consultar el historial de atenciones.

---

## 🏗️ Estructuras de Datos Utilizadas

| Estructura | Rol en el Sistema | Complejidad Principal |
|---|---|---|
| **MaxHeap** | Cola de prioridad — despacha al paciente más crítico primero | Inserción/Extracción: O(log n) |
| **BST** (Árbol Binario de Búsqueda) | Índice de pacientes por ID — búsqueda eficiente | Búsqueda/Inserción: O(log n) promedio |
| **Map[String, List[String]]** | Historial de atenciones agrupado por especialidad | Acceso: O(1) amortizado |
| **List[String]** | Log cronológico inmutable de todos los eventos | Append: O(1) amortizado |

---

## 🚀 Cómo Ejecutar

### Requisitos
- **Scala** 2.13+ o 3.x
- **SBT** (Scala Build Tool) — opcional, se puede compilar directamente con `scalac`

### Compilar y ejecutar con `scalac`

```bash
# Compilar
scalac src/main/scala/TriageHospital.scala -d out/

# Ejecutar
scala -cp out/ TriageHospital
```

### Con SBT (si se configura `build.sbt`)

```bash
sbt run
```

---

## 📁 Estructura del Proyecto

```
proyecto-scala/
├── src/
│   └── main/
│       └── scala/
│           └── TriageHospital.scala   # Implementación principal
├── diagrams/
│   └── uml_sistema_triaje.xml         # Diagrama UML (draw.io)
├── docs/
│   └── Entrega3_EstructurasDatos.docx # Documento del informe
├── .gitignore
└── README.md
```

---

## 🧩 Arquitectura

### Clases Principales

```
Paciente (case class)
    ↓ almacenado en
BSTPacientes ──── NodoBST
    +
MaxHeapPacientes
    +
SistemaTriaje (orquestador)
    ├── Map[String, List[String]]  (historial)
    └── List[String]               (log)
```

### Flujo de operaciones

1. `registrarPaciente(p)` → inserta en **MaxHeap** y en **BST**
2. `atenderSiguiente(especialidad)` → extrae del **MaxHeap**, elimina del **BST**, actualiza **Map**
3. `buscarPaciente(id)` → consulta el **BST** en O(log n)
4. `listarPacientesPorId()` → recorrido in-order del **BST**

---

## 💡 ¿Por qué Scala para este problema?

- **Inmutabilidad por defecto**: `List` y `Map` son inmutables → sin efectos secundarios accidentales
- **Case classes**: `Paciente` es un modelo de datos limpio y seguro
- **Pattern matching**: reemplaza cascadas de `if/else`, hace el código del BST expresivo y conciso
- **Option[T]**: elimina los `NullPointerException`; `buscar()` devuelve `Option[Paciente]`
- **Interoperabilidad con JVM**: hereda todas las optimizaciones de la JVM para aplicaciones de alto rendimiento

---

## 📊 Salida Esperada (fragmento)

```
============================================================
   SISTEMA DE TRIAJE HOSPITALARIO — Scala Demo
============================================================

--- Fase 1: Registro de pacientes ---
✔ Registrado: Paciente[101] María López (prioridad=3, síntoma='Fiebre alta')
✔ Registrado: Paciente[205] Carlos Gómez (prioridad=5, síntoma='Paro cardíaco')
...

--- Fase 3: Atención de pacientes ---
⏭  Próximo a atender: Paciente[205] Carlos Gómez (prioridad=5, ...)
🏥 Atendido por Urgencias: Carlos Gómez (prioridad=5)
🏥 Atendido por Urgencias: José Herrera (prioridad=5)
🏥 Atendido por Traumatología: Pedro Ramírez (prioridad=4)
...
```

---

## 📄 Licencia

Proyecto académico — Universidad del Valle de Guatemala, 2025.
