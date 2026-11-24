package org.example;

import javax.swing.*; // Componentes gráficos de Java Swing.
import javax.swing.border.TitledBorder; // Bordes con título.
import javax.swing.table.DefaultTableCellRenderer; // Personaliza las celdas de una tabla.
import javax.swing.table.DefaultTableModel; // Administra los datos de una tabla.
import javax.swing.table.JTableHeader; // Personaliza el encabezado de una tabla.
import java.awt.*; // Colores, fuentes y diseño de componentes.
import java.awt.event.KeyAdapter; // Detecta eventos del teclado.
import java.awt.event.KeyEvent; // Identifica las teclas presionadas.
import java.awt.print.PrinterException; // Maneja errores de impresión.
import java.io.IOException; // Maneja errores de archivos.
import java.math.BigDecimal; // Trabaja con decimales precisos.
import java.math.RoundingMode; // Establece el tipo de redondeo.
import java.nio.charset.StandardCharsets; // Define la codificación de texto.
import java.nio.file.Files; // Lee y escribe archivos.
import java.time.LocalDateTime; // Obtiene la fecha y hora actual.
import java.time.format.DateTimeFormatter; // Da formato a fechas y horas.
import java.util.ArrayList; // Crea listas dinámicas.
import java.util.Comparator; // Permite comparar y ordenar datos.
import java.util.LinkedHashMap; // Guarda datos en pares clave-valor y conserva el orden.
import java.util.List; // Trabaja con listas de elementos.
import java.util.Locale; // Configura formatos según la región.
import java.util.Map; // Almacena datos mediante claves y valores.
import java.util.function.Consumer; // Ejecuta acciones que reciben un dato.

public class Main extends JFrame {

    private static final DateTimeFormatter FORMATO_RELOJ = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss");

    private final Carrito carrito = new Carrito();
    private final List<Venta> ventas = new ArrayList<>();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Producto", "Precio", "Cant.", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    private final JComboBox<Carrito.Servicio> comboServicio = new JComboBox<>(Carrito.Servicio.values());
    private final JComboBox<String> comboPago = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Yape", "Plin"});
    private final JTextField txtPromo = new JTextField(10);
    private final JTextField txtCliente = new JTextField(16);
    private final JTextField txtDocumento = new JTextField(9);
    private final JTextField txtBuscar = new JTextField(20);
    private final JPanel panelTodos = new JPanel(new BorderLayout());

    private final JLabel lblReloj = new JLabel();
    private final JLabel lblItems = new JLabel();
    private final JLabel lblSubtotal = new JLabel();
    private final JLabel lblDescuento = new JLabel();
    private final JLabel lblEnvio = new JLabel();
    private final JLabel lblAvisoEnvio = new JLabel(" ");
    private final JLabel lblIgv = new JLabel();
    private final JLabel lblTotal = new JLabel();

    public Main() {
        super("KFC - Sistema de Ventas");
        setSize(1300, 800);
        setMinimumSize(new Dimension(1150, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Tema.CREMA);
        setLayout(new BorderLayout(0, 8));

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(crearCabecera(), BorderLayout.NORTH);
        norte.add(crearBarraOpciones(), BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        JPanel carritoPanel = crearPanelCarrito();
        carritoPanel.setPreferredSize(new Dimension(490, 0));

        JPanel centro = new JPanel(new BorderLayout(12, 0));
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        centro.add(crearPanelProductos(), BorderLayout.CENTER);
        centro.add(carritoPanel, BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);

        add(crearPanelInferior(), BorderLayout.SOUTH);

        refrescarTodos();
        refrescar();
        setVisible(true);
    }

    private JPanel crearCabecera() {
        PanelDegradado cabecera = new PanelDegradado(Tema.ROJO, Tema.ROJO_OSCURO);
        cabecera.setLayout(new BorderLayout());
        cabecera.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));

        JLabel logo = new JLabel("KFC");
        logo.setFont(Tema.LOGO);
        logo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("SISTEMA DE VENTAS  |  CAJA 01");
        subtitulo.setFont(Tema.NEGRITA);
        subtitulo.setForeground(Tema.DORADO);

        JPanel izquierda = new JPanel();
        izquierda.setOpaque(false);
        izquierda.setLayout(new BoxLayout(izquierda, BoxLayout.Y_AXIS));
        izquierda.add(logo);
        izquierda.add(subtitulo);

        lblReloj.setFont(new Font("Arial", Font.BOLD, 18));
        lblReloj.setForeground(Color.WHITE);
        lblReloj.setText(LocalDateTime.now().format(FORMATO_RELOJ));
        new javax.swing.Timer(1000, e -> lblReloj.setText(LocalDateTime.now().format(FORMATO_RELOJ))).start();

        cabecera.add(izquierda, BorderLayout.WEST);
        cabecera.add(lblReloj, BorderLayout.EAST);
        return cabecera;
    }

    private JPanel crearBarraOpciones() {
        comboServicio.setFont(Tema.NORMAL);
        comboPago.setFont(Tema.NORMAL);
        for (JTextField campo : new JTextField[]{txtPromo, txtCliente, txtDocumento, txtBuscar}) {
            campo.setFont(Tema.NORMAL);
        }

        comboServicio.addActionListener(e -> {
            carrito.setServicio((Carrito.Servicio) comboServicio.getSelectedItem());
            refrescar();
        });

        BotonRedondeado btnPromo = new BotonRedondeado("Aplicar promo", Tema.VERDE);
        BotonRedondeado btnComparar = new BotonRedondeado("Comparar opciones", Tema.DORADO);
        BotonRedondeado btnHistorial = new BotonRedondeado("Ventas del día", Tema.AZUL);
        btnPromo.addActionListener(e -> aplicarPromo());
        btnComparar.addActionListener(e -> abrirComparador());
        btnHistorial.addActionListener(e -> new DialogoHistorial(this, ventas).setVisible(true));

        JLabel ayudaPromo = new JLabel("Promos: BIENVENIDA10 · 2X1POWER · FAMILIA15");
        ayudaPromo.setFont(new Font("Arial", Font.ITALIC, 12));
        ayudaPromo.setForeground(Tema.GRIS);

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        fila1.setOpaque(false);
        fila1.add(etiqueta("Servicio:"));
        fila1.add(comboServicio);
        fila1.add(etiqueta("Cliente:"));
        fila1.add(txtCliente);
        fila1.add(etiqueta("DNI:"));
        fila1.add(txtDocumento);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        fila2.setOpaque(false);
        fila2.add(etiqueta("Código de promo:"));
        fila2.add(txtPromo);
        fila2.add(btnPromo);
        fila2.add(ayudaPromo);
        fila2.add(Box.createHorizontalStrut(30));
        fila2.add(btnComparar);
        fila2.add(btnHistorial);

        JPanel barra = new JPanel(new GridLayout(2, 1));
        barra.setBackground(Tema.CREMA);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Tema.DORADO));
        barra.add(fila1);
        barra.add(fila2);
        return barra;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.NEGRITA);
        l.setForeground(Tema.ROJO_OSCURO);
        return l;
    }

    private JPanel crearPanelProductos() {
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(new Font("Arial", Font.BOLD, 14));

        pestanas.addTab("Todos", panelTodos);
        pestanas.setBackgroundAt(0, Tema.ROJO);
        pestanas.setForegroundAt(0, Color.WHITE);

        int indice = 1;
        for (String categoria : Menu.CATEGORIAS) {
            pestanas.addTab(categoria, envolver(crearGrid(Menu.porCategoria(categoria))));
            Color color = Tema.colorCategoria(categoria);
            pestanas.setBackgroundAt(indice, color);
            pestanas.setForegroundAt(indice, Tema.textoSobre(color));
            indice++;
        }

        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refrescarTodos();
                pestanas.setSelectedIndex(0);
            }
        });
        BotonRedondeado limpiar = new BotonRedondeado("Limpiar", Tema.GRIS);
        limpiar.addActionListener(e -> {
            txtBuscar.setText("");
            refrescarTodos();
        });

        JPanel buscador = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        buscador.setOpaque(false);
        buscador.add(etiqueta("Buscar producto:"));
        buscador.add(txtBuscar);
        buscador.add(limpiar);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(titulo("MENÚ DE PRODUCTOS", Tema.ROJO));
        panel.add(buscador, BorderLayout.NORTH);
        panel.add(pestanas, BorderLayout.CENTER);
        return panel;
    }

    private TitledBorder titulo(String texto, Color color) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(color, 2, true), " " + texto + " ",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), color);
    }

    private JPanel crearGrid(List<Producto> productos) {
        JPanel grid = new JPanel(new GridLayout(0, 3, 10, 10));
        grid.setOpaque(false);
        if (productos.isEmpty()) {
            JLabel vacio = new JLabel("No se encontraron productos", JLabel.CENTER);
            vacio.setFont(Tema.NEGRITA);
            vacio.setForeground(Tema.GRIS);
            JPanel contenedor = new JPanel(new BorderLayout());
            contenedor.setOpaque(false);
            contenedor.add(vacio, BorderLayout.CENTER);
            contenedor.setPreferredSize(new Dimension(300, 120));
            return contenedor;
        }
        for (Producto p : productos) {
            TarjetaProducto tarjeta = new TarjetaProducto(p);
            tarjeta.addActionListener(e -> {
                carrito.agregar(p);
                refrescar();
            });
            grid.add(tarjeta);
        }
        return grid;
    }

    private JScrollPane envolver(JPanel contenido) {
        JPanel fondo = new JPanel(new BorderLayout());
        fondo.setBackground(Tema.CREMA);
        fondo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        fondo.add(contenido, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(fondo);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getViewport().setBackground(Tema.CREMA);
        return scroll;
    }

    private void refrescarTodos() {
        String filtro = txtBuscar.getText().trim().toLowerCase();
        List<Producto> resultado = Menu.buscarTexto(filtro);
        panelTodos.removeAll();
        panelTodos.add(envolver(crearGrid(resultado)), BorderLayout.CENTER);
        panelTodos.revalidate();
        panelTodos.repaint();
    }

    private JPanel crearPanelCarrito() {
        BotonRedondeado btnMas = new BotonRedondeado("+", Tema.VERDE);
        BotonRedondeado btnMenos = new BotonRedondeado("−", Tema.ROJO);
        BotonRedondeado btnQuitar = new BotonRedondeado("Quitar", Tema.ROJO_OSCURO);
        btnMas.addActionListener(e -> cambiarCantidad(+1));
        btnMenos.addActionListener(e -> cambiarCantidad(-1));
        btnQuitar.addActionListener(e -> quitarSeleccionado());

        lblItems.setFont(Tema.NEGRITA);
        lblItems.setForeground(Tema.ROJO_OSCURO);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        acciones.setOpaque(false);
        acciones.add(btnMas);
        acciones.add(btnMenos);
        acciones.add(btnQuitar);
        acciones.add(Box.createHorizontalStrut(12));
        acciones.add(lblItems);

        configurarTabla();
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.setBorder(BorderFactory.createLineBorder(Tema.ROJO, 1));

        for (JLabel l : new JLabel[]{lblSubtotal, lblDescuento, lblEnvio, lblIgv}) {
            l.setFont(Tema.NORMAL_GRANDE);
            l.setForeground(Tema.NEGRO);
        }
        lblAvisoEnvio.setFont(new Font("Arial", Font.ITALIC, 12));
        lblAvisoEnvio.setForeground(Tema.AZUL);

        JLabel textoTotal = new JLabel("TOTAL A PAGAR");
        textoTotal.setFont(new Font("Arial", Font.BOLD, 16));
        textoTotal.setForeground(Color.WHITE);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 30));
        lblTotal.setForeground(Tema.DORADO);

        PanelDegradado bandaTotal = new PanelDegradado(Tema.ROJO, Tema.ROJO_OSCURO);
        bandaTotal.setLayout(new BorderLayout());
        bandaTotal.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        bandaTotal.add(textoTotal, BorderLayout.WEST);
        bandaTotal.add(lblTotal, BorderLayout.EAST);

        JPanel resumen = new JPanel(new GridLayout(0, 1, 0, 3));
        resumen.setOpaque(false);
        resumen.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        resumen.add(lblSubtotal);
        resumen.add(lblDescuento);
        resumen.add(lblEnvio);
        resumen.add(lblAvisoEnvio);
        resumen.add(lblIgv);

        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        sur.add(resumen, BorderLayout.CENTER);
        sur.add(bandaTotal, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                titulo("CARRITO DE COMPRAS", Tema.ROJO), BorderFactory.createEmptyBorder(4, 8, 8, 8)));
        panel.add(acciones, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);
        panel.add(sur, BorderLayout.SOUTH);
        return panel;
    }

    private void configurarTabla() {
        tabla.setRowHeight(32);
        tabla.setFont(Tema.NORMAL_GRANDE);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setSelectionBackground(Tema.DORADO);
        tabla.setSelectionForeground(Tema.NEGRO);
        tabla.setDefaultRenderer(Object.class, new RenderizadorFilas());

        JTableHeader cabecera = tabla.getTableHeader();
        cabecera.setReorderingAllowed(false);
        cabecera.setBackground(Tema.ROJO);
        cabecera.setForeground(Color.WHITE);
        cabecera.setFont(Tema.NEGRITA);
        cabecera.setPreferredSize(new Dimension(0, 34));

        tabla.getColumnModel().getColumn(0).setPreferredWidth(210);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(90);
    }

    private JPanel crearPanelInferior() {
        comboPago.setFont(Tema.NEGRITA);

        JLabel textoPago = new JLabel("Método de pago:");
        textoPago.setFont(Tema.NEGRITA);
        textoPago.setForeground(Color.WHITE);

        BotonRedondeado btnCobrar = new BotonRedondeado("COBRAR Y GENERAR BOLETA", Tema.VERDE);
        btnCobrar.setFont(new Font("Arial", Font.BOLD, 16));
        btnCobrar.setPreferredSize(new Dimension(320, 46));
        BotonRedondeado btnReimprimir = new BotonRedondeado("Ver última boleta", Tema.DORADO);
        BotonRedondeado btnVaciar = new BotonRedondeado("Vaciar pedido", Tema.ROJO);

        btnCobrar.addActionListener(e -> cobrar());
        btnReimprimir.addActionListener(e -> verUltimaBoleta());
        btnVaciar.addActionListener(e -> vaciarConConfirmacion());

        PanelDegradado panel = new PanelDegradado(Tema.NEGRO, new Color(70, 20, 25));
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 10));
        panel.add(textoPago);
        panel.add(comboPago);
        panel.add(btnCobrar);
        panel.add(btnReimprimir);
        panel.add(btnVaciar);
        return panel;
    }

    private void aplicarPromo() {
        try {
            carrito.aplicarPromo(txtPromo.getText());
            refrescar();
            if (carrito.promo() == Carrito.Promo.NINGUNA) {
                avisar("Se quitó la promoción.");
            } else if (carrito.descuento().signum() > 0) {
                avisar("Promo aplicada: " + carrito.promo().descripcion());
            } else {
                avisar("Promo registrada, pero aún no se cumplen sus condiciones:\n"
                        + carrito.promo().descripcion());
            }
        } catch (IllegalArgumentException ex) {
            avisar(ex.getMessage());
        }
    }

    private Producto productoSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            avisar("Seleccione un producto del carrito.");
            return null;
        }
        return carrito.lineas().get(fila).producto();
    }

    private void cambiarCantidad(int delta) {
        int fila = tabla.getSelectedRow();
        Producto producto = productoSeleccionado();
        if (producto == null) {
            return;
        }
        carrito.cambiarCantidad(producto, delta);
        refrescar();
        if (fila < modelo.getRowCount()) {
            tabla.setRowSelectionInterval(fila, fila);
        }
    }

    private void quitarSeleccionado() {
        Producto producto = productoSeleccionado();
        if (producto == null) {
            return;
        }
        carrito.eliminar(producto);
        refrescar();
    }

    private void vaciarConConfirmacion() {
        if (carrito.estaVacio()) {
            avisar("El pedido ya está vacío.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Desea vaciar todo el pedido?",
                "Vaciar pedido", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            nuevoPedido();
        }
    }

    private void nuevoPedido() {
        carrito.vaciar();
        txtPromo.setText("");
        txtCliente.setText("");
        txtDocumento.setText("");
        refrescar();
    }

    private void abrirComparador() {
        new DialogoComparador(this, producto -> {
            carrito.agregar(producto);
            refrescar();
        }).setVisible(true);
    }

    private BigDecimal pedirMontoEfectivo() {
        String texto = JOptionPane.showInputDialog(this,
                "Total a pagar: " + soles(carrito.total()) + "\nMonto recibido (S/):",
                "Pago en efectivo", JOptionPane.QUESTION_MESSAGE);
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            BigDecimal recibido = new BigDecimal(texto.trim().replace(',', '.'));
            carrito.vuelto(recibido);
            return recibido;
        } catch (NumberFormatException ex) {
            avisar("Ingrese un monto válido, por ejemplo 50 o 50.50");
        } catch (IllegalArgumentException ex) {
            avisar(ex.getMessage());
        }
        return null;
    }

    private void cobrar() {
        if (carrito.estaVacio()) {
            avisar("El carrito está vacío.");
            return;
        }
        String documento = txtDocumento.getText().trim();
        if (!documento.isEmpty() && !documento.matches("\\d{8}")) {
            avisar("El DNI debe tener exactamente 8 dígitos.");
            return;
        }
        String metodo = (String) comboPago.getSelectedItem();
        BigDecimal recibido = null;
        if ("Efectivo".equals(metodo)) {
            recibido = pedirMontoEfectivo();
            if (recibido == null) {
                return;
            }
        }
        String cliente = txtCliente.getText().trim().isEmpty() ? "Cliente varios" : txtCliente.getText().trim();
        String numero = String.format(Locale.US, "B001-%06d", ventas.size() + 1);
        LocalDateTime ahora = LocalDateTime.now();
        DatosVenta datos = new DatosVenta(numero, ahora, cliente,
                documento.isEmpty() ? "-" : documento, metodo, recibido);

        Venta venta = new Venta(numero, ahora, cliente, carrito.servicio().toString(), metodo,
                carrito.total(), BoletaFormatter.generarTexto(carrito, datos),
                BoletaFormatter.generarHtml(carrito, datos));
        ventas.add(venta);
        new DialogoBoleta(this, venta).setVisible(true);
        nuevoPedido();
    }

    private void verUltimaBoleta() {
        if (ventas.isEmpty()) {
            avisar("Aún no se ha generado ninguna boleta.");
            return;
        }
        new DialogoBoleta(this, ventas.get(ventas.size() - 1)).setVisible(true);
    }

    private void refrescar() {
        modelo.setRowCount(0);
        for (Carrito.Linea linea : carrito.lineas()) {
            modelo.addRow(new Object[]{linea.producto().nombre(), soles(linea.producto().precio()),
                    linea.cantidad(), soles(linea.subtotal())});
        }
        lblItems.setText("Productos: " + carrito.cantidadTotal());
        lblSubtotal.setText("Subtotal: " + soles(carrito.subtotal()));

        boolean hayDescuento = carrito.descuento().signum() > 0;
        lblDescuento.setText("Descuento (" + carrito.promo().descripcion() + "): -" + soles(carrito.descuento()));
        lblDescuento.setForeground(hayDescuento ? Tema.VERDE_OSCURO : Tema.NEGRO);

        lblEnvio.setText("Servicio " + carrito.servicio() + " - envío: " + soles(carrito.costoEnvio()));
        BigDecimal falta = carrito.faltanteEnvioGratis();
        lblAvisoEnvio.setText(falta.signum() > 0
                ? "Agregue " + soles(falta) + " más y el delivery es GRATIS"
                : " ");
        lblIgv.setText("IGV (18%) incluido: " + soles(carrito.igvIncluido()));
        lblTotal.setText(soles(carrito.total()));
    }

    static String soles(BigDecimal monto) {
        return String.format(Locale.US, "S/ %.2f", monto);
    }

    private void avisar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "KFC", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
        }
        SwingUtilities.invokeLater(Main::new);
    }
}

final class Tema {
    static final Color ROJO = new Color(200, 16, 46);
    static final Color ROJO_OSCURO = new Color(140, 0, 25);
    static final Color CREMA = new Color(255, 247, 237);
    static final Color NEGRO = new Color(34, 34, 34);
    static final Color DORADO = new Color(255, 193, 7);
    static final Color VERDE = new Color(46, 160, 67);
    static final Color VERDE_OSCURO = new Color(25, 110, 45);
    static final Color AZUL = new Color(30, 110, 190);
    static final Color GRIS = new Color(120, 120, 120);
    static final Color FILA_PAR = new Color(255, 236, 232);

    static final Font LOGO = new Font("Arial", Font.BOLD | Font.ITALIC, 40);
    static final Font NEGRITA = new Font("Arial", Font.BOLD, 13);
    static final Font NORMAL = new Font("Arial", Font.PLAIN, 13);
    static final Font NORMAL_GRANDE = new Font("Arial", Font.PLAIN, 15);

    private Tema() {
    }

    static Color colorCategoria(String categoria) {
        return switch (categoria) {
            case "Combos" -> ROJO;
            case "Baldes" -> ROJO_OSCURO;
            case "Sándwiches" -> new Color(230, 120, 20);
            case "Pollo" -> new Color(175, 70, 30);
            case "Complementos" -> DORADO;
            case "Bebidas" -> AZUL;
            case "Postres" -> new Color(210, 70, 140);
            case "Niños" -> VERDE;
            default -> GRIS;
        };
    }

    static Color textoSobre(Color fondo) {
        double luz = 0.299 * fondo.getRed() + 0.587 * fondo.getGreen() + 0.114 * fondo.getBlue();
        return luz > 170 ? NEGRO : Color.WHITE;
    }
}

class PanelDegradado extends JPanel {

    private final Color inicio;
    private final Color fin;

    PanelDegradado(Color inicio, Color fin) {
        this.inicio = inicio;
        this.fin = fin;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new GradientPaint(0, 0, inicio, getWidth(), 0, fin));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}

class BotonRedondeado extends JButton {

    private final Color fondo;

    BotonRedondeado(String texto, Color fondo) {
        super(texto);
        this.fondo = fondo;
        setForeground(Tema.textoSobre(fondo));
        setFont(Tema.NEGRITA);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 16, 8, 16));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color color;
        if (!isEnabled()) {
            color = Color.LIGHT_GRAY;
        } else if (getModel().isPressed()) {
            color = fondo.darker();
        } else if (getModel().isRollover()) {
            color = fondo.brighter();
        } else {
            color = fondo;
        }
        g2.setColor(color);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        g2.dispose();
        super.paintComponent(g);
    }
}

class TarjetaProducto extends JButton {

    private final Producto producto;
    private final Color fondo;

    TarjetaProducto(Producto producto) {
        this.producto = producto;
        this.fondo = Tema.colorCategoria(producto.categoria());
        setPreferredSize(new Dimension(170, 100));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(producto.nombre() + " - " + producto.categoria());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();
        Color base = getModel().isPressed() ? fondo.darker() : getModel().isRollover() ? fondo.brighter() : fondo;
        g2.setPaint(new GradientPaint(0, 0, base, 0, alto, base.darker()));
        g2.fillRoundRect(0, 0, ancho - 1, alto - 1, 24, 24);
        g2.setColor(new Color(255, 255, 255, 120));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(1, 1, ancho - 3, alto - 3, 24, 24);

        Color texto = Tema.textoSobre(fondo);
        g2.setColor(texto);
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int y = 22;
        for (String linea : partir(producto.nombre(), fm, ancho - 24)) {
            g2.drawString(linea, 12, y);
            y += 16;
        }

        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString(String.format(Locale.US, "S/ %.2f", producto.precio()), 12, alto - 14);

        if (producto.piezas() > 0) {
            String etiqueta = producto.piezas() + " pzs";
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            FontMetrics pm = g2.getFontMetrics();
            int w = pm.stringWidth(etiqueta) + 14;
            g2.setColor(new Color(255, 255, 255, 230));
            g2.fillRoundRect(ancho - w - 8, alto - 30, w, 20, 14, 14);
            g2.setColor(fondo.darker());
            g2.drawString(etiqueta, ancho - w - 1, alto - 16);
        }
        g2.dispose();
    }

    private static List<String> partir(String texto, FontMetrics fm, int maximo) {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (fm.stringWidth(prueba) <= maximo || actual.length() == 0) {
                actual = new StringBuilder(prueba);
            } else {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            }
        }
        lineas.add(actual.toString());
        return lineas.size() > 2 ? lineas.subList(0, 2) : lineas;
    }
}

class RenderizadorFilas extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                   boolean foco, int fila, int columna) {
        Component c = super.getTableCellRendererComponent(tabla, valor, seleccionada, false, fila, columna);
        if (!seleccionada) {
            c.setBackground(fila % 2 == 0 ? Color.WHITE : Tema.FILA_PAR);
            c.setForeground(Tema.NEGRO);
        }
        setHorizontalAlignment(columna == 0 ? LEFT : CENTER);
        return c;
    }
}

record DatosVenta(String numero, LocalDateTime fecha, String cliente, String documento,
                  String metodo, BigDecimal recibido) {
}

record Venta(String numero, LocalDateTime fecha, String cliente, String servicio, String metodo,
             BigDecimal total, String texto, String html) {
}

class DialogoBoleta extends JDialog {

    private final JEditorPane vista = new JEditorPane();
    private final Venta venta;

    DialogoBoleta(Frame padre, Venta venta) {
        super(padre, "Boleta " + venta.numero(), true);
        this.venta = venta;

        vista.setContentType("text/html");
        vista.setText(venta.html());
        vista.setEditable(false);
        vista.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(vista);
        scroll.setPreferredSize(new Dimension(480, 580));
        scroll.setBorder(BorderFactory.createLineBorder(Tema.ROJO, 2));

        BotonRedondeado btnImprimir = new BotonRedondeado("Imprimir", Tema.VERDE);
        BotonRedondeado btnGuardar = new BotonRedondeado("Guardar archivo", Tema.DORADO);
        BotonRedondeado btnCerrar = new BotonRedondeado("Cerrar", Tema.ROJO);
        btnImprimir.addActionListener(e -> imprimir());
        btnGuardar.addActionListener(e -> guardar());
        btnCerrar.addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        botones.setBackground(Tema.CREMA);
        botones.add(btnImprimir);
        botones.add(btnGuardar);
        botones.add(btnCerrar);

        setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.CREMA);
        add(scroll, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(padre);
    }

    private void imprimir() {
        try {
            vista.print();
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo imprimir la boleta.", "KFC",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardar() {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new java.io.File("boleta-" + venta.numero() + ".html"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            String contenido = "<meta charset=\"UTF-8\">" + venta.html();
            Files.writeString(selector.getSelectedFile().toPath(), contenido, StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this, "Boleta guardada correctamente.", "KFC",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo.", "KFC",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

class DialogoHistorial extends JDialog {

    DialogoHistorial(Frame padre, List<Venta> ventas) {
        super(padre, "Ventas del día", true);

        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Boleta", "Hora", "Cliente", "Servicio", "Pago", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        DateTimeFormatter hora = DateTimeFormatter.ofPattern("HH:mm:ss");
        BigDecimal suma = BigDecimal.ZERO;
        for (Venta v : ventas) {
            modelo.addRow(new Object[]{v.numero(), v.fecha().format(hora), v.cliente(), v.servicio(),
                    v.metodo(), Main.soles(v.total())});
            suma = suma.add(v.total());
        }

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setFont(Tema.NORMAL_GRANDE);
        tabla.setShowGrid(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setSelectionBackground(Tema.DORADO);
        tabla.setSelectionForeground(Tema.NEGRO);
        tabla.setDefaultRenderer(Object.class, new RenderizadorFilas());
        tabla.getTableHeader().setBackground(Tema.ROJO);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(Tema.NEGRITA);
        tabla.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(700, 320));
        scroll.setBorder(BorderFactory.createLineBorder(Tema.ROJO, 2));

        JLabel resumen = new JLabel(ventas.size() + " ventas  |  Total vendido: " + Main.soles(suma));
        resumen.setFont(new Font("Arial", Font.BOLD, 16));
        resumen.setForeground(Tema.ROJO_OSCURO);

        BotonRedondeado btnVer = new BotonRedondeado("Ver boleta", Tema.AZUL);
        BotonRedondeado btnCerrar = new BotonRedondeado("Cerrar", Tema.ROJO);
        btnVer.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione una venta de la lista.", "KFC",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            new DialogoBoleta(padre, ventas.get(fila)).setVisible(true);
        });
        btnCerrar.addActionListener(e -> dispose());

        JPanel inferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        inferior.setBackground(Tema.CREMA);
        inferior.add(resumen);
        inferior.add(btnVer);
        inferior.add(btnCerrar);

        setLayout(new BorderLayout(0, 8));
        getContentPane().setBackground(Tema.CREMA);
        add(scroll, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(padre);
    }
}

class DialogoComparador extends JDialog {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Puesto", "Producto", "Categoría", "Precio", "Piezas", "S/ por pieza", "Ahorro"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JComboBox<String> comboCategoria = new JComboBox<>();
    private final JLabel lblMejor = new JLabel();
    private final Consumer<Producto> alAgregar;
    private List<Comparador.Fila> filasActuales = List.of();

    DialogoComparador(Frame padre, Consumer<Producto> alAgregar) {
        super(padre, "Comparador de opciones", true);
        this.alAgregar = alAgregar;

        comboCategoria.addItem("Todas");
        for (String categoria : Menu.categoriasConPiezas()) {
            comboCategoria.addItem(categoria);
        }
        comboCategoria.setFont(Tema.NEGRITA);
        comboCategoria.addActionListener(e -> cargar());

        JLabel instruccion = new JLabel("Categoría:");
        instruccion.setFont(Tema.NEGRITA);
        instruccion.setForeground(Tema.ROJO_OSCURO);

        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        superior.setBackground(Tema.CREMA);
        superior.add(instruccion);
        superior.add(comboCategoria);

        configurarTabla();
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(820, 360));
        scroll.setBorder(BorderFactory.createLineBorder(Tema.ROJO, 2));

        lblMejor.setFont(new Font("Arial", Font.BOLD, 15));
        lblMejor.setForeground(Tema.VERDE_OSCURO);

        BotonRedondeado btnAgregar = new BotonRedondeado("Agregar al pedido", Tema.VERDE);
        BotonRedondeado btnCerrar = new BotonRedondeado("Cerrar", Tema.ROJO);
        btnAgregar.addActionListener(e -> agregarSeleccionado());
        btnCerrar.addActionListener(e -> dispose());

        JPanel inferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        inferior.setBackground(Tema.CREMA);
        inferior.add(lblMejor);
        inferior.add(btnAgregar);
        inferior.add(btnCerrar);

        setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.CREMA);
        add(superior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);

        cargar();
        pack();
        setLocationRelativeTo(padre);
    }

    private void configurarTabla() {
        tabla.setRowHeight(30);
        tabla.setFont(Tema.NORMAL_GRANDE);
        tabla.setShowGrid(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setSelectionBackground(Tema.DORADO);
        tabla.setSelectionForeground(Tema.NEGRO);
        tabla.getTableHeader().setBackground(Tema.ROJO);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(Tema.NEGRITA);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(210);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                                                           boolean foco, int fila, int columna) {
                Component c = super.getTableCellRendererComponent(t, valor, sel, false, fila, columna);
                setHorizontalAlignment(columna == 1 || columna == 2 ? LEFT : CENTER);
                if (!sel) {
                    int ultima = t.getRowCount() - 1;
                    if (fila == 0) {
                        c.setBackground(new Color(198, 239, 206));
                        c.setFont(Tema.NEGRITA);
                    } else if (fila == ultima && ultima > 0) {
                        c.setBackground(new Color(255, 205, 210));
                        c.setFont(Tema.NORMAL_GRANDE);
                    } else {
                        c.setBackground(fila % 2 == 0 ? Color.WHITE : Tema.FILA_PAR);
                        c.setFont(Tema.NORMAL_GRANDE);
                    }
                    c.setForeground(Tema.NEGRO);
                }
                return c;
            }
        });
    }

    private void cargar() {
        String seleccion = (String) comboCategoria.getSelectedItem();
        List<Producto> base = Menu.conPiezas().stream()
                .filter(p -> "Todas".equals(seleccion) || p.categoria().equals(seleccion))
                .toList();
        filasActuales = Comparador.porPrecioPorPieza(base);

        modelo.setRowCount(0);
        if (filasActuales.isEmpty()) {
            lblMejor.setText(" ");
            return;
        }
        BigDecimal maximo = filasActuales.get(filasActuales.size() - 1).precioPorPieza();
        int puesto = 1;
        for (Comparador.Fila fila : filasActuales) {
            BigDecimal ahorro = maximo.subtract(fila.precioPorPieza())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(maximo, 0, RoundingMode.HALF_UP);
            modelo.addRow(new Object[]{puesto++, fila.producto().nombre(), fila.producto().categoria(),
                    Main.soles(fila.producto().precio()), fila.producto().piezas(),
                    Main.soles(fila.precioPorPieza()), ahorro.signum() == 0 ? "-" : ahorro + " %"});
        }
        Comparador.Fila mejor = filasActuales.get(0);
        lblMejor.setText("Mejor precio por pieza: " + mejor.producto().nombre()
                + " (" + Main.soles(mejor.precioPorPieza()) + ")");
    }

    private void agregarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una opción de la tabla.", "KFC",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Producto producto = filasActuales.get(fila).producto();
        alAgregar.accept(producto);
        JOptionPane.showMessageDialog(this, producto.nombre() + " agregado al pedido.", "KFC",
                JOptionPane.INFORMATION_MESSAGE);
    }
}

class Carrito {

    static final BigDecimal IGV = new BigDecimal("0.18");
    static final BigDecimal COSTO_DELIVERY = new BigDecimal("5.00");
    static final BigDecimal DELIVERY_GRATIS_DESDE = new BigDecimal("60.00");
    static final BigDecimal FAMILIA_MINIMO = new BigDecimal("100.00");

    enum Servicio {
        LOCAL("En local"), LLEVAR("Para llevar"), DRIVE_THRU("Drive-thru"), DELIVERY("Delivery");

        private final String etiqueta;

        Servicio(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    enum Promo {
        NINGUNA("", "Sin promo"),
        BIENVENIDA10("BIENVENIDA10", "10 % de descuento en el subtotal"),
        DOS_X_UNO_POWER("2X1POWER", "2x1 en Combo Power Krunch"),
        FAMILIA15("FAMILIA15", "15 % en compras desde S/ 100");

        private final String codigo;
        private final String descripcion;

        Promo(String codigo, String descripcion) {
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        String codigo() {
            return codigo;
        }

        String descripcion() {
            return descripcion;
        }

        static Promo desdeCodigo(String texto) {
            String limpio = texto == null ? "" : texto.trim().toUpperCase();
            for (Promo p : values()) {
                if (p.codigo.equals(limpio)) {
                    return p;
                }
            }
            throw new IllegalArgumentException("La promo '" + texto + "' no existe");
        }
    }

    record Linea(Producto producto, int cantidad) {
        BigDecimal subtotal() {
            return dinero(producto.precio().multiply(BigDecimal.valueOf(cantidad)));
        }
    }

    private final Map<Producto, Integer> items = new LinkedHashMap<>();
    private Servicio servicio = Servicio.LOCAL;
    private Promo promo = Promo.NINGUNA;

    void agregar(Producto producto) {
        agregar(producto, 1);
    }

    void agregar(Producto producto, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        items.merge(producto, cantidad, Integer::sum);
    }

    void cambiarCantidad(Producto producto, int delta) {
        if (!items.containsKey(producto)) {
            throw new IllegalArgumentException(producto.nombre() + " no está en el carrito");
        }
        int nueva = items.get(producto) + delta;
        if (nueva <= 0) {
            items.remove(producto);
        } else {
            items.put(producto, nueva);
        }
    }

    void eliminar(Producto producto) {
        if (items.remove(producto) == null) {
            throw new IllegalArgumentException(producto.nombre() + " no está en el carrito");
        }
    }

    void vaciar() {
        items.clear();
        promo = Promo.NINGUNA;
    }

    boolean estaVacio() {
        return items.isEmpty();
    }

    int cantidadTotal() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    List<Linea> lineas() {
        List<Linea> lista = new ArrayList<>();
        items.forEach((p, c) -> lista.add(new Linea(p, c)));
        return lista;
    }

    Servicio servicio() {
        return servicio;
    }

    void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    Promo promo() {
        return promo;
    }

    void aplicarPromo(String codigo) {
        this.promo = Promo.desdeCodigo(codigo);
    }

    BigDecimal subtotal() {
        BigDecimal suma = BigDecimal.ZERO;
        for (Linea l : lineas()) {
            suma = suma.add(l.subtotal());
        }
        return dinero(suma);
    }

    BigDecimal descuento() {
        return switch (promo) {
            case BIENVENIDA10 -> dinero(subtotal().multiply(new BigDecimal("0.10")));
            case FAMILIA15 -> subtotal().compareTo(FAMILIA_MINIMO) >= 0
                    ? dinero(subtotal().multiply(new BigDecimal("0.15")))
                    : dinero(BigDecimal.ZERO);
            case DOS_X_UNO_POWER -> descuentoDosPorUno();
            case NINGUNA -> dinero(BigDecimal.ZERO);
        };
    }

    private BigDecimal descuentoDosPorUno() {
        int cantidad = 0;
        BigDecimal precio = BigDecimal.ZERO;
        for (Linea l : lineas()) {
            if (l.producto().codigo().equals(Menu.CODIGO_POWER_KRUNCH)) {
                cantidad = l.cantidad();
                precio = l.producto().precio();
            }
        }
        return dinero(precio.multiply(BigDecimal.valueOf(cantidad / 2)));
    }

    BigDecimal costoEnvio() {
        if (servicio != Servicio.DELIVERY) {
            return dinero(BigDecimal.ZERO);
        }
        BigDecimal pagaProductos = subtotal().subtract(descuento());
        if (pagaProductos.compareTo(DELIVERY_GRATIS_DESDE) >= 0) {
            return dinero(BigDecimal.ZERO);
        }
        return COSTO_DELIVERY;
    }

    BigDecimal faltanteEnvioGratis() {
        if (servicio != Servicio.DELIVERY || estaVacio()) {
            return dinero(BigDecimal.ZERO);
        }
        BigDecimal falta = DELIVERY_GRATIS_DESDE.subtract(subtotal().subtract(descuento()));
        return falta.signum() > 0 ? dinero(falta) : dinero(BigDecimal.ZERO);
    }

    BigDecimal total() {
        return dinero(subtotal().subtract(descuento()).add(costoEnvio()));
    }

    BigDecimal igvIncluido() {
        return total().multiply(IGV).divide(BigDecimal.ONE.add(IGV), 2, RoundingMode.HALF_UP);
    }

    BigDecimal baseImponible() {
        return total().subtract(igvIncluido());
    }

    BigDecimal vuelto(BigDecimal recibido) {
        if (recibido == null || recibido.compareTo(total()) < 0) {
            throw new IllegalArgumentException("El monto recibido no alcanza para pagar el total");
        }
        return dinero(recibido.subtract(total()));
    }

    static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}

record Producto(String codigo, String nombre, String categoria, BigDecimal precio, int piezas) {

    static Producto of(String codigo, String nombre, String categoria, String precio, int piezas) {
        return new Producto(codigo, nombre, categoria, new BigDecimal(precio), piezas);
    }
}

final class Menu {

    static final String CODIGO_POWER_KRUNCH = "C006";

    static final List<String> CATEGORIAS = List.of(
            "Combos", "Baldes", "Sándwiches", "Pollo", "Complementos", "Bebidas", "Postres", "Niños");

    private static final List<Producto> PRODUCTOS = List.of(
            Producto.of("C001", "Combo Clásico", "Combos", "33.80", 2),
            Producto.of("C002", "Combo 8 Nuggets", "Combos", "38.20", 8),
            Producto.of("C003", "Combo Hot Wings", "Combos", "39.00", 6),
            Producto.of("C004", "Combo Tenders", "Combos", "44.20", 4),
            Producto.of("C006", "Combo Power Krunch", "Combos", "19.90", 2),
            Producto.of("C007", "Combo Personal 2 piezas", "Combos", "29.90", 2),
            Producto.of("C008", "Combo Dúo 4 piezas", "Combos", "54.90", 4),
            Producto.of("C009", "Combo Tres Piezas", "Combos", "36.90", 3),
            Producto.of("C010", "Combo Zinger", "Combos", "27.90", 0),
            Producto.of("C011", "Combo Big Crunch", "Combos", "26.90", 0),
            Producto.of("C012", "Combo Krunchy Meltz", "Combos", "32.90", 0),
            Producto.of("C013", "Combo Twister", "Combos", "24.90", 0),

            Producto.of("C005", "Balde 8 piezas", "Baldes", "79.90", 8),
            Producto.of("C014", "Balde 6 piezas", "Baldes", "62.90", 6),
            Producto.of("C015", "Balde 12 piezas", "Baldes", "109.90", 12),
            Producto.of("C016", "Balde 16 piezas", "Baldes", "139.90", 16),
            Producto.of("C017", "Balde Familiar 10 piezas + papas", "Baldes", "99.90", 10),
            Producto.of("C018", "Balde Tenders 15 piezas", "Baldes", "89.90", 15),

            Producto.of("S001", "Big Crunch", "Sándwiches", "18.90", 0),
            Producto.of("S002", "Krunchy Meltz", "Sándwiches", "25.70", 0),
            Producto.of("S003", "Zinger", "Sándwiches", "22.90", 0),
            Producto.of("S004", "Twister", "Sándwiches", "17.90", 0),
            Producto.of("S005", "Sándwich Clásico", "Sándwiches", "20.90", 0),
            Producto.of("S006", "Wrap Crispy", "Sándwiches", "16.90", 0),
            Producto.of("S007", "Doble Crunch", "Sándwiches", "28.90", 0),

            Producto.of("L001", "1 pieza de pollo", "Pollo", "9.50", 1),
            Producto.of("L002", "2 piezas de pollo", "Pollo", "18.00", 2),
            Producto.of("L003", "3 piezas de pollo", "Pollo", "26.00", 3),
            Producto.of("L004", "Tenders x3", "Pollo", "17.90", 3),
            Producto.of("L005", "Tenders x5", "Pollo", "27.90", 5),
            Producto.of("L006", "Hot Wings x6", "Pollo", "24.90", 6),
            Producto.of("L007", "Hot Wings x10", "Pollo", "38.90", 10),
            Producto.of("L008", "Nuggets x10", "Pollo", "17.90", 10),
            Producto.of("L009", "Nuggets x20", "Pollo", "32.90", 20),

            Producto.of("X001", "Papas", "Complementos", "9.90", 0),
            Producto.of("X002", "Nuggets", "Complementos", "12.00", 0),
            Producto.of("X003", "Pop Corn", "Complementos", "15.00", 0),
            Producto.of("X004", "Ensalada", "Complementos", "5.90", 0),
            Producto.of("X005", "Puré de papas", "Complementos", "6.50", 0),
            Producto.of("X006", "Papas grandes", "Complementos", "13.90", 0),
            Producto.of("X007", "Arroz", "Complementos", "6.50", 0),
            Producto.of("X008", "Mazorca", "Complementos", "6.90", 0),
            Producto.of("X009", "Salsa extra", "Complementos", "2.50", 0),

            Producto.of("B001", "Gaseosa", "Bebidas", "7.00", 0),
            Producto.of("B002", "Chicha morada", "Bebidas", "8.00", 0),
            Producto.of("B003", "Gaseosa grande", "Bebidas", "10.50", 0),
            Producto.of("B004", "Agua mineral", "Bebidas", "5.00", 0),
            Producto.of("B005", "Limonada frozen", "Bebidas", "11.90", 0),
            Producto.of("B006", "Té helado", "Bebidas", "7.50", 0),
            Producto.of("B007", "Café", "Bebidas", "6.50", 0),
            Producto.of("B008", "Jarra de chicha", "Bebidas", "22.00", 0),

            Producto.of("P001", "Brownie", "Postres", "7.90", 0),
            Producto.of("P002", "Helado", "Postres", "5.50", 0),
            Producto.of("P003", "Sundae", "Postres", "8.90", 0),
            Producto.of("P004", "Pie de manzana", "Postres", "6.90", 0),
            Producto.of("P005", "Torta tres leches", "Postres", "9.50", 0),
            Producto.of("P006", "Galleta", "Postres", "3.90", 0),

            Producto.of("N001", "Kids Nuggets x4", "Niños", "22.90", 4),
            Producto.of("N002", "Kids Tenders x2", "Niños", "23.90", 2),
            Producto.of("N003", "Kids 1 pieza", "Niños", "21.90", 1),
            Producto.of("N004", "Kids Pop Corn", "Niños", "20.90", 0)
    );

    private Menu() {
    }

    static List<Producto> todos() {
        return PRODUCTOS;
    }

    static List<Producto> porCategoria(String categoria) {
        return PRODUCTOS.stream().filter(p -> p.categoria().equals(categoria)).toList();
    }

    static List<Producto> conPiezas() {
        return PRODUCTOS.stream().filter(p -> p.piezas() > 0).toList();
    }

    static List<String> categoriasConPiezas() {
        return CATEGORIAS.stream()
                .filter(c -> PRODUCTOS.stream().anyMatch(p -> p.categoria().equals(c) && p.piezas() > 0))
                .toList();
    }

    static List<Producto> buscarTexto(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        return PRODUCTOS.stream()
                .filter(p -> p.nombre().toLowerCase().contains(filtro)
                        || p.categoria().toLowerCase().contains(filtro))
                .toList();
    }

    static Producto buscar(String codigo) {
        return PRODUCTOS.stream()
                .filter(p -> p.codigo().equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto " + codigo));
    }
}

final class Comparador {

    record Fila(Producto producto, BigDecimal precioPorPieza) {
    }

    private Comparador() {
    }

    static List<Fila> porPrecioPorPieza(List<Producto> productos) {
        return productos.stream()
                .map(p -> {
                    if (p.piezas() <= 0) {
                        throw new IllegalArgumentException(p.nombre() + " no tiene piezas para comparar");
                    }
                    BigDecimal porPieza = p.precio()
                            .divide(BigDecimal.valueOf(p.piezas()), 2, RoundingMode.HALF_UP);
                    return new Fila(p, porPieza);
                })
                .sorted(Comparator.comparing(Fila::precioPorPieza))
                .toList();
    }
}

final class BoletaFormatter {

    private static final String DOBLE = "====================================\n";
    private static final String SIMPLE = "------------------------------------\n";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private BoletaFormatter() {
    }

    static String generar(Carrito c, String metodoPago, BigDecimal recibido, LocalDateTime fecha) {
        return generarTexto(c, new DatosVenta("B001-000000", fecha, "Cliente varios", "-", metodoPago, recibido));
    }

    static String generarTexto(Carrito c, DatosVenta d) {
        StringBuilder b = new StringBuilder();
        b.append(DOBLE);
        b.append("               KFC PERU\n");
        b.append("        BOLETA DE VENTA ELECTRONICA\n");
        b.append("             ").append(d.numero()).append("\n");
        b.append(DOBLE);
        b.append("Fecha: ").append(d.fecha().format(FECHA)).append("\n");
        b.append("Cliente: ").append(d.cliente()).append("\n");
        b.append("DNI: ").append(d.documento()).append("\n");
        b.append("Servicio: ").append(c.servicio()).append("\n");
        b.append(SIMPLE);
        b.append(String.format(Locale.US, "%-18s %4s %10s%n", "Producto", "Cant", "Importe"));
        b.append(SIMPLE);

        for (Carrito.Linea l : c.lineas()) {
            b.append(String.format(Locale.US, "%-18s %4d %10.2f%n",
                    cortar(l.producto().nombre(), 18), l.cantidad(), l.subtotal()));
        }

        b.append(SIMPLE);
        b.append(fila("Subtotal:", c.subtotal()));
        if (c.descuento().signum() > 0) {
            b.append(fila("Descuento (" + c.promo().codigo() + "):", c.descuento().negate()));
        }
        if (c.costoEnvio().signum() > 0) {
            b.append(fila("Delivery:", c.costoEnvio()));
        }
        b.append(fila("Base imponible:", c.baseImponible()));
        b.append(fila("IGV (18%) incluido:", c.igvIncluido()));
        b.append(fila("TOTAL:", c.total()));
        b.append(SIMPLE);
        b.append("Pago: ").append(d.metodo()).append("\n");
        if (d.recibido() != null) {
            b.append(fila("Recibido:", d.recibido()));
            b.append(fila("Vuelto:", c.vuelto(d.recibido())));
        }
        b.append(DOBLE);
        b.append("        GRACIAS POR SU COMPRA\n");
        b.append(DOBLE);
        return b.toString();
    }

    static String generarHtml(Carrito c, DatosVenta d) {
        StringBuilder h = new StringBuilder();
        h.append("<html><body bgcolor=\"#FFFFFF\"><font face=\"Arial\" size=\"3\">");

        h.append("<table width=\"100%\" cellpadding=\"8\" cellspacing=\"0\"><tr>");
        h.append("<td align=\"center\" bgcolor=\"#C8102E\">");
        h.append("<font color=\"#FFFFFF\" size=\"7\"><b>KFC</b></font><br>");
        h.append("<font color=\"#FFFFFF\" size=\"3\"><b>BOLETA DE VENTA ELECTRONICA</b></font><br>");
        h.append("<font color=\"#FFD54F\" size=\"4\"><b>").append(esc(d.numero())).append("</b></font>");
        h.append("</td></tr></table><br>");

        h.append("<table width=\"100%\" cellpadding=\"2\" cellspacing=\"0\">");
        h.append(filaDato("Fecha", d.fecha().format(FECHA)));
        h.append(filaDato("Cliente", d.cliente()));
        h.append(filaDato("DNI", d.documento()));
        h.append(filaDato("Servicio", c.servicio().toString()));
        h.append("</table><br>");

        h.append("<table width=\"100%\" cellpadding=\"4\" cellspacing=\"0\">");
        h.append("<tr bgcolor=\"#8C0019\">");
        h.append(celda("Producto", "left", "#FFFFFF", true));
        h.append(celda("Cant", "center", "#FFFFFF", true));
        h.append(celda("P. Unit", "right", "#FFFFFF", true));
        h.append(celda("Importe", "right", "#FFFFFF", true));
        h.append("</tr>");
        int indice = 0;
        for (Carrito.Linea l : c.lineas()) {
            String fondo = indice++ % 2 == 0 ? "#FFFFFF" : "#FFECE8";
            h.append("<tr bgcolor=\"").append(fondo).append("\">");
            h.append(celda(l.producto().nombre(), "left", "#222222", false));
            h.append(celda(String.valueOf(l.cantidad()), "center", "#222222", false));
            h.append(celda(num(l.producto().precio()), "right", "#222222", false));
            h.append(celda(num(l.subtotal()), "right", "#222222", false));
            h.append("</tr>");
        }
        h.append("</table><br>");

        h.append("<table width=\"100%\" cellpadding=\"3\" cellspacing=\"0\">");
        h.append(filaTotal("Subtotal", "S/ " + num(c.subtotal()), "#222222"));
        if (c.descuento().signum() > 0) {
            h.append(filaTotal("Descuento " + c.promo().codigo(), "- S/ " + num(c.descuento()), "#1E7A32"));
        }
        if (c.costoEnvio().signum() > 0) {
            h.append(filaTotal("Delivery", "S/ " + num(c.costoEnvio()), "#222222"));
        }
        h.append(filaTotal("Base imponible", "S/ " + num(c.baseImponible()), "#555555"));
        h.append(filaTotal("IGV (18%) incluido", "S/ " + num(c.igvIncluido()), "#555555"));
        h.append("<tr><td align=\"right\" bgcolor=\"#C8102E\"><font color=\"#FFFFFF\" size=\"5\"><b>TOTAL</b></font></td>");
        h.append("<td align=\"right\" bgcolor=\"#C8102E\"><font color=\"#FFFFFF\" size=\"5\"><b>S/ ")
                .append(num(c.total())).append("</b></font></td></tr>");
        h.append("</table><br>");

        h.append("<table width=\"100%\" cellpadding=\"2\" cellspacing=\"0\">");
        h.append(filaDato("Pago", d.metodo()));
        if (d.recibido() != null) {
            h.append(filaDato("Recibido", "S/ " + num(d.recibido())));
            h.append(filaDato("Vuelto", "S/ " + num(c.vuelto(d.recibido()))));
        }
        h.append("</table><br>");

        h.append("<table width=\"100%\" cellpadding=\"8\" cellspacing=\"0\"><tr>");
        h.append("<td align=\"center\" bgcolor=\"#FFC107\"><font color=\"#222222\" size=\"4\">");
        h.append("<b>GRACIAS POR SU COMPRA</b></font><br>");
        h.append("<font color=\"#222222\" size=\"2\">Conserve su boleta de venta</font>");
        h.append("</td></tr></table>");

        h.append("</font></body></html>");
        return h.toString();
    }

    private static String filaDato(String etiqueta, String valor) {
        return "<tr><td width=\"30%\"><font color=\"#8C0019\"><b>" + esc(etiqueta) + ":</b></font></td><td>"
                + esc(valor) + "</td></tr>";
    }

    private static String filaTotal(String etiqueta, String valor, String color) {
        return "<tr><td align=\"right\"><font color=\"" + color + "\">" + esc(etiqueta)
                + "</font></td><td align=\"right\"><font color=\"" + color + "\">" + esc(valor)
                + "</font></td></tr>";
    }

    private static String celda(String contenido, String alineacion, String color, boolean negrita) {
        String texto = negrita ? "<b>" + esc(contenido) + "</b>" : esc(contenido);
        return "<td align=\"" + alineacion + "\"><font color=\"" + color + "\">" + texto + "</font></td>";
    }

    private static String num(BigDecimal valor) {
        return String.format(Locale.US, "%.2f", valor);
    }

    private static String esc(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String fila(String etiqueta, BigDecimal valor) {
        return String.format(Locale.US, "%-24s S/ %8.2f%n", etiqueta, valor);
    }

    private static String cortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max - 1) + ".";
    }
}