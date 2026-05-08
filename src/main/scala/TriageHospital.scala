package TriageHospital

/**
 * Sistema de Triaje Hospitalario
 *
 *
 * Entrega 3 - Algoritmos y Estructura de Datos
 * Universidad del Valle de Guatemala
 * Integrantes: Franco Paiz 25780 / Junior Lancerio 25789
 *
 * Estructuras de datos utilizadas:
 *   - MaxHeap (Cola de prioridad) : despacho de pacientes por urgencia
 *   - BST (Árbol Binario de Búsqueda): búsqueda de pacientes por ID
 *   - Map[String, List[String]]  : historial de atenciones por especialidad
 *   - List[String]               : registro de log de eventos
 */

// ─────────────────────────────────────────────
// Modelo de dominio
// ─────────────────────────────────────────────

/**
 * Representa a un paciente en el sistema de triaje.
 *
 * @param id        Identificador único del paciente
 * @param nombre    Nombre completo
 * @param prioridad Nivel de urgencia (1=bajo … 5=crítico)
 * @param sintoma   Descripción breve del síntoma principal
 */
case class Paciente(id: Int, nombre: String, prioridad: Int, sintoma: String) {
  require(prioridad >= 1 && prioridad <= 5, "La prioridad debe estar entre 1 y 5")
  override def toString: String =
    s"Paciente[$id] $nombre (prioridad=$prioridad, síntoma='$sintoma')"
}

// ─────────────────────────────────────────────
// Nodo del BST
// ─────────────────────────────────────────────

/**
 * Nodo interno del árbol BST.
 * Cada nodo almacena un Paciente y referencias a sus hijos.
 */
class NodoBST(val paciente: Paciente) {
  var izquierdo: Option[NodoBST] = None
  var derecho: Option[NodoBST]   = None
}

// ─────────────────────────────────────────────
// BST  – Árbol Binario de Búsqueda por ID
// ─────────────────────────────────────────────

/**
 * Árbol Binario de Búsqueda (BST) que indexa pacientes por su ID numérico.
 *
 * Complejidades:
 *   - Inserción   : O(h)  donde h = altura del árbol
 *   - Búsqueda    : O(h)
 *   - Recorrido   : O(n)
 *
 * En el caso promedio con datos aleatorios h ≈ log₂(n).
 */
class BSTPacientes {

  private var raiz: Option[NodoBST] = None

  /** Inserta un paciente. Si el ID ya existe, lo reemplaza. */
  def insertar(p: Paciente): Unit = {
    raiz = Some(insertarRec(raiz, p))
  }

  private def insertarRec(nodo: Option[NodoBST], p: Paciente): NodoBST = {
    nodo match {
      case None =>
        new NodoBST(p)                                   // posición encontrada
      case Some(n) if p.id < n.paciente.id =>
        n.izquierdo = Some(insertarRec(n.izquierdo, p))  // ir a la izquierda
        n
      case Some(n) if p.id > n.paciente.id =>
        n.derecho = Some(insertarRec(n.derecho, p))       // ir a la derecha
        n
      case Some(n) =>
        new NodoBST(p)                                   // reemplazar duplicado
    }
  }

  /**
   * Busca un paciente por ID.
   * @return Some(Paciente) si se encuentra, None en caso contrario.
   */
  def buscar(id: Int): Option[Paciente] = buscarRec(raiz, id)

  private def buscarRec(nodo: Option[NodoBST], id: Int): Option[Paciente] =
    nodo match {
      case None                             => None
      case Some(n) if id == n.paciente.id  => Some(n.paciente)
      case Some(n) if id < n.paciente.id   => buscarRec(n.izquierdo, id)
      case Some(n)                          => buscarRec(n.derecho, id)
    }

  /**
   * Elimina un paciente por ID (algoritmo estándar BST con sucesor in-order).
   */
  def eliminar(id: Int): Unit = {
    raiz = eliminarRec(raiz, id)
  }

  private def eliminarRec(nodo: Option[NodoBST], id: Int): Option[NodoBST] =
    nodo match {
      case None => None
      case Some(n) if id < n.paciente.id =>
        n.izquierdo = eliminarRec(n.izquierdo, id); Some(n)
      case Some(n) if id > n.paciente.id =>
        n.derecho = eliminarRec(n.derecho, id); Some(n)
      case Some(n) =>
        // Nodo encontrado: tres casos
        (n.izquierdo, n.derecho) match {
          case (None, None) => None                      // hoja
          case (None, r)    => r                         // solo hijo derecho
          case (l, None)    => l                         // solo hijo izquierdo
          case (_, Some(r)) =>                           // dos hijos → sucesor in-order
            val sucesor = minimoNodo(r)
            val nuevoNodo = new NodoBST(sucesor.paciente)
            nuevoNodo.izquierdo = n.izquierdo
            nuevoNodo.derecho   = eliminarRec(n.derecho, sucesor.paciente.id)
            Some(nuevoNodo)
        }
    }

  /** Devuelve el nodo con el valor mínimo a partir de un nodo dado. */
  private def minimoNodo(nodo: NodoBST): NodoBST =
    nodo.izquierdo match {
      case None    => nodo
      case Some(i) => minimoNodo(i)
    }

  /** Recorrido in-order (IDs en orden ascendente). */
  def inOrder(): List[Paciente] = inOrderRec(raiz)

  private def inOrderRec(nodo: Option[NodoBST]): List[Paciente] =
    nodo match {
      case None    => List()
      case Some(n) => inOrderRec(n.izquierdo) ::: List(n.paciente) ::: inOrderRec(n.derecho)
    }

  /** Altura del árbol (útil para diagnosticar balanceo). */
  def altura(): Int = alturaRec(raiz)

  private def alturaRec(nodo: Option[NodoBST]): Int =
    nodo match {
      case None    => 0
      case Some(n) => 1 + math.max(alturaRec(n.izquierdo), alturaRec(n.derecho))
    }
}

// ─────────────────────────────────────────────
// MaxHeap – Cola de prioridad por urgencia
// ─────────────────────────────────────────────

/**
 * MaxHeap implementado sobre un arreglo mutable (Array).
 * El elemento con MAYOR prioridad siempre está en la raíz (índice 0).
 *
 * Complejidades:
 *   - Inserción (heapify-up)   : O(log n)
 *   - Extracción del máximo    : O(log n)
 *   - Consulta del máximo      : O(1)
 */
class MaxHeapPacientes(capacidad: Int) {

  private val heap = new Array[Paciente](capacidad)
  private var tamanio = 0

  /** Agrega un paciente al heap. */
  def insertar(p: Paciente): Unit = {
    if (tamanio >= capacidad) throw new RuntimeException("Heap lleno")
    heap(tamanio) = p
    tamanio += 1
    subirNodo(tamanio - 1)           // restaurar propiedad del heap
  }

  /** Extrae y retorna al paciente con mayor prioridad. */
  def extraerMaximo(): Paciente = {
    if (tamanio == 0) throw new RuntimeException("Heap vacío")
    val raiz = heap(0)
    heap(0) = heap(tamanio - 1)      // mover último a la raíz
    tamanio -= 1
    bajarNodo(0)                     // restaurar propiedad del heap
    raiz
  }

  /** Consulta el máximo sin extraerlo. */
  def verMaximo(): Option[Paciente] =
    if (tamanio == 0) None else Some(heap(0))

  def estaVacio: Boolean = tamanio == 0
  def cantidad: Int      = tamanio

  // Subir un nodo hasta que la propiedad del heap se restaure
  private def subirNodo(idx: Int): Unit = {
    if (idx == 0) return
    val padre = (idx - 1) / 2
    if (heap(idx).prioridad > heap(padre).prioridad) {
      intercambiar(idx, padre)
      subirNodo(padre)
    }
  }

  // Bajar un nodo hasta que la propiedad del heap se restaure
  private def bajarNodo(idx: Int): Unit = {
    val izq = 2 * idx + 1
    val der = 2 * idx + 2
    var mayor = idx

    if (izq < tamanio && heap(izq).prioridad > heap(mayor).prioridad) mayor = izq
    if (der < tamanio && heap(der).prioridad > heap(mayor).prioridad) mayor = der

    if (mayor != idx) {
      intercambiar(idx, mayor)
      bajarNodo(mayor)
    }
  }

  private def intercambiar(i: Int, j: Int): Unit = {
    val tmp = heap(i); heap(i) = heap(j); heap(j) = tmp
  }
}

// ─────────────────────────────────────────────
// Sistema de Triaje (orquesta todas las estructuras)
// ─────────────────────────────────────────────

/**
 * Sistema central de triaje.
 *
 * Combina tres estructuras para ofrecer funcionalidades complementarias:
 *   1. MaxHeap  → despacha al paciente más urgente en O(log n)
 *   2. BST      → búsqueda/eliminación de un paciente por ID en O(log n) promedio
 *   3. Map      → historial de atenciones agrupadas por especialidad en O(1) amortizado
 *   4. List     → log inmutable de eventos (append con :+)
 */
class SistemaTriaje {

  private val colaPrioridad = new MaxHeapPacientes(200)
  private val indiceBST     = new BSTPacientes
  private var historial     = Map[String, List[String]]()   // especialidad → lista de eventos
  private var logEventos    = List[String]()                 // log cronológico inmutable

  // ── Registro ──────────────────────────────────

  /**
   * Registra un nuevo paciente en el sistema.
   * Lo agrega al MaxHeap (para despacho) y al BST (para búsqueda).
   */
  def registrarPaciente(p: Paciente): Unit = {
    colaPrioridad.insertar(p)
    indiceBST.insertar(p)
    val evento = s"[REGISTRO] ${p.nombre} (ID=${p.id}, prioridad=${p.prioridad})"
    logEventos = logEventos :+ evento
    println(s" Registrado: $p")
  }

  // ── Despacho ──────────────────────────────────

  /**
   * Atiende al paciente más urgente (extrae del MaxHeap) y actualiza el historial.
   *
   * @param especialidad Área médica que atiende al paciente (ej. "Urgencias", "Cardiología")
   */
  def atenderSiguiente(especialidad: String): Unit = {
    if (colaPrioridad.estaVacio) {
      println("⚠ No hay pacientes en espera.")
      return
    }
    val p      = colaPrioridad.extraerMaximo()
    val evento = s"Atendido por $especialidad: ${p.nombre} (prioridad=${p.prioridad})"

    // Actualizar Map de historial (inmutable → crear nueva entrada)
    val listaActual = historial.getOrElse(especialidad, List())
    historial       = historial.updated(especialidad, listaActual :+ evento)
    logEventos      = logEventos :+ s"[ATENCION] $evento"

    // Eliminar del BST porque ya fue atendido
    indiceBST.eliminar(p.id)

    println(s" $evento")
  }

  // ── Consultas ─────────────────────────────────

  /** Busca un paciente por ID usando el BST. */
  def buscarPaciente(id: Int): Unit = {
    indiceBST.buscar(id) match {
      case Some(p) => println(s" Encontrado: $p")
      case None    => println(s" No se encontró paciente con ID=$id")
    }
  }

  /** Muestra todos los pacientes en espera ordenados por ID (recorrido in-order del BST). */
  def listarPacientesPorId(): Unit = {
    val lista = indiceBST.inOrder()
    if (lista.isEmpty) println(" No hay pacientes registrados.")
    else {
      println("\n Pacientes en espera (orden por ID):")
      lista.foreach(p => println(s"   $p"))
    }
  }

  /** Muestra el siguiente en ser atendido sin extraerlo. */
  def verSiguiente(): Unit = {
    colaPrioridad.verMaximo() match {
      case Some(p) => println(s"⏭  Próximo a atender: $p")
      case None    => println("⏭  Cola vacía.")
    }
  }

  /** Imprime el historial de atenciones por especialidad. */
  def mostrarHistorial(): Unit = {
    println("\n Historial por especialidad:")
    if (historial.isEmpty) println("   (sin atenciones aún)")
    historial.foreach { case (esp, eventos) =>
      println(s"\n  [$esp]")
      eventos.foreach(e => println(s"    • $e"))
    }
  }

  /** Imprime el log cronológico completo de eventos. */
  def mostrarLog(): Unit = {
    println("\n🗒  Log de eventos:")
    logEventos.foreach(e => println(s"  $e"))
  }

  /** Estadísticas rápidas del sistema. */
  def estadisticas(): Unit = {
    println(s"\n Estadísticas:")
    println(s"   Pacientes en cola de prioridad : ${colaPrioridad.cantidad}")
    println(s"   Altura del BST                 : ${indiceBST.altura()}")
    println(s"   Especialidades con historial   : ${historial.size}")
    println(s"   Eventos en log                 : ${logEventos.size}")
  }
}

// ─────────────────────────────────────────────
// Punto de entrada
// ─────────────────────────────────────────────

object TriageHospital extends App {

  println("=" * 60)
  println("   SISTEMA DE TRIAJE HOSPITALARIO — Scala Demo")
  println("=" * 60)

  val sistema = new SistemaTriaje

  // ── Fase 1: Registro de pacientes ─────────────
  println("\n--- Fase 1: Registro de pacientes ---")

  sistema.registrarPaciente(Paciente(101, "María López",    3, "Fiebre alta"))
  sistema.registrarPaciente(Paciente(205, "Carlos Gómez",   5, "Paro cardíaco"))
  sistema.registrarPaciente(Paciente(342, "Ana Torres",     2, "Dolor de cabeza"))
  sistema.registrarPaciente(Paciente(188, "Pedro Ramírez",  4, "Fractura de brazo"))
  sistema.registrarPaciente(Paciente(77,  "Lucía Mendez",   1, "Consulta rutinaria"))
  sistema.registrarPaciente(Paciente(310, "José Herrera",   5, "Hemorragia interna"))
  sistema.registrarPaciente(Paciente(99,  "Elena Castillo", 3, "Dificultad respiratoria"))

  // ── Fase 2: Consultas sobre el BST ────────────
  println("\n--- Fase 2: Consultas BST ---")
  sistema.listarPacientesPorId()
  sistema.buscarPaciente(188)
  sistema.buscarPaciente(999)

  // ── Fase 3: Atención (MaxHeap en acción) ──────
  println("\n--- Fase 3: Atención de pacientes ---")
  sistema.verSiguiente()
  sistema.atenderSiguiente("Urgencias")
  sistema.atenderSiguiente("Urgencias")
  sistema.atenderSiguiente("Traumatología")
  sistema.atenderSiguiente("Medicina General")

  // ── Fase 4: Estado posterior ───────────────────
  println("\n--- Fase 4: Estado del sistema tras atenciones ---")
  sistema.listarPacientesPorId()
  sistema.verSiguiente()

  // ── Fase 5: Historial y log ────────────────────
  println("\n--- Fase 5: Historial y log ---")
  sistema.mostrarHistorial()
  sistema.mostrarLog()

  // ── Fase 6: Estadísticas ───────────────────────
  sistema.estadisticas()

  println("\n" + "=" * 60)
  println("   Demo finalizado.")
  println("=" * 60)
}


