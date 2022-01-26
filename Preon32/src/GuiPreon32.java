import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;

public class GuiPreon32 {

	// atribut untuk UI
	private JFrame frame;
	private JPanel panel;
	private JButton connectButton, uploadButton, restartButton, checkComponentButton, sensorSuhuButton,
			sensorTekananUdaraButton, sensorKelembabanUdaraButton, sensorGetaranButton, downloadButton;
	private static JTextArea log;
	private JComboBox<String> portListed;
	private JTextPane serialPort, baudRate, dataBits, parityBit, stopBit, flowControl;

	private AppController controller;

	private String msg;

	/**
	 * Launch the application.
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiPreon32 window = new GuiPreon32();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * Create the application.
	 */
	public GuiPreon32() {
		this.controller = new AppController(); // list of preon32 COM itu terdeteksi
		this.print("\n");
		initialize();
		this.checkComponentButton.setEnabled(false);
		this.uploadButton.setEnabled(false);
		this.downloadButton.setEnabled(false);
		this.restartButton.setEnabled(false);
		this.sensorGetaranButton.setEnabled(false);
		this.sensorSuhuButton.setEnabled(false);
		this.sensorKelembabanUdaraButton.setEnabled(false);
		this.sensorTekananUdaraButton.setEnabled(false);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		// inisialisasi awal untuk frame
		frame = new JFrame();
		frame.setTitle("Preon32 GUI");
		frame.setBounds(100, 100, 770, 506);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		// inisialisasi untuk Tab pannel
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(23, 11, 337, 298);
		frame.getContentPane().add(tabbedPane);

		// inisialisasi untuk pannel yang ada didalam tab pannel
		panel = new JPanel();
		tabbedPane.addTab("Information", null, panel, null);
		panel.setLayout(null);

		// inisialisasi untuk bagian UI sebelah kiri

		// inisialisasi button connect/disconnect
		connectButton = new JButton("Connect");
		connectButton.setBounds(140, 28, 139, 23);
		panel.add(connectButton);

		// menambahkan action listener untuk connect button
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					connectButtonPress();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		// inisialisasi combo box (untuk list sensor yang terbaca)
		portListed = new JComboBox<String>();
		portListed.setBounds(44, 28, 86, 22);
		panel.add(portListed); // panel.add(this.listSerialPorts) // ini sebenarnya memasukkan combobox ke
								// frame

		// memanggil fungsi untuk menampilkan list port yang ada
		this.addToListSerialPort(this.controller.getListPort()); // method utk menaruh
																	// this.listSerialPort(this.listSerialPorts)

		// inisialisasi untuk UI bagian tengah (informasi)

		// inisialisasi label Serial Port
		JLabel serialPortLabel = new JLabel("Serial Port:");
		serialPortLabel.setBounds(44, 61, 82, 20);
		panel.add(serialPortLabel);

		// inisialisasi label Baud Rate
		JLabel baudRateLabel = new JLabel("Baud Rate:");
		baudRateLabel.setBounds(44, 92, 82, 20);
		panel.add(baudRateLabel);

		// inisialisasi label Data Bits
		JLabel dataBitLabel = new JLabel("Data Bits:");
		dataBitLabel.setBounds(44, 123, 82, 20);
		panel.add(dataBitLabel);

		// inisialisasi label Parity
		JLabel parityLabel = new JLabel("Parity Bit:");
		parityLabel.setBounds(44, 154, 82, 20);
		panel.add(parityLabel);

//		 inisialisasi label Stop bits
		JLabel stopBitLabel = new JLabel("Stop Bit:");
		stopBitLabel.setBounds(44, 185, 82, 20);
		panel.add(stopBitLabel);

		// inisialisasi label Flow Control
		JLabel flowControldLabel = new JLabel("Flow Control:");
		flowControldLabel.setBounds(44, 216, 82, 20);
		panel.add(flowControldLabel);

		// inisialisasi text pane untuk menampilkan informasi
		serialPort = new JTextPane();
		baudRate = new JTextPane();
		dataBits = new JTextPane();
		parityBit = new JTextPane();
		stopBit = new JTextPane();
		flowControl = new JTextPane();

		// memanggil fungsi untuk mengisi nilai
		setValue("", "0", "0", "0", "0", "0");

		// inisialisasi untuk UI bagian kanan

		checkComponentButton = new JButton("Check Component");
		checkComponentButton.setBounds(23, 391, 162, 23);
		frame.getContentPane().add(checkComponentButton);

		// menambahkan action listener untuk restart button
		//
		checkComponentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					checkComponentButtonPress();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		// inisialisasi untuk UI bagian bawah

		// inisialisasi untuk Scoll Pane
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(393, 36, 337, 412);
		frame.getContentPane().add(scrollPane);

		// inisialisasi untuk Text Are (log)
		log = new JTextArea();
		scrollPane.setViewportView(log);
		DefaultCaret caret = (DefaultCaret) log.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		uploadButton = new JButton("Upload File");
		uploadButton.setBounds(23, 320, 162, 23);
		frame.getContentPane().add(uploadButton);

		// menambahkan action listener untuk upload button
		uploadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					uploadButtonPress();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});

		downloadButton = new JButton("Download File");
		downloadButton.setBounds(23, 357, 162, 23);
		frame.getContentPane().add(downloadButton);
		downloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					downloadButtonPress();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		sensorTekananUdaraButton = new JButton("Sensor Tekanan");
		sensorTekananUdaraButton.setBounds(195, 391, 165, 23);
		frame.getContentPane().add(sensorTekananUdaraButton);

		sensorTekananUdaraButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sensorButtonPress(6);
			}
		});

		sensorKelembabanUdaraButton = new JButton("Sensor Kelembaban");
		sensorKelembabanUdaraButton.setBounds(195, 425, 165, 23);
		frame.getContentPane().add(sensorKelembabanUdaraButton);

		sensorKelembabanUdaraButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sensorButtonPress(7);
			}
		});

		sensorGetaranButton = new JButton("Sensor Getaran");
		sensorGetaranButton.setBounds(195, 357, 165, 23);
		frame.getContentPane().add(sensorGetaranButton);

		restartButton = new JButton("Restart");
		restartButton.setBounds(23, 425, 162, 23);
		frame.getContentPane().add(restartButton);

		sensorSuhuButton = new JButton("Sensor Suhu");
		sensorSuhuButton.setBounds(195, 320, 165, 23);
		frame.getContentPane().add(sensorSuhuButton);

		sensorSuhuButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sensorButtonPress(5);
			}
		});

		// menambahkan action listener untuk restart button
		restartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				restartButtonPress();
			}
		});

		sensorGetaranButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sensorButtonPress(8);
			}
		});

	}

	/**
	 * 
	 * @param com         nama sensor
	 * @param res         nilai baud rate
	 * @param res2        nilai data bit
	 * @param res3        nilai parity
	 * @param isiStopBits nilai stop bit
	 * @param res5        nilai Flow Control
	 * 
	 *                    method ini berfungsi untuk merubah nilai pada UI
	 *                    berdasarkan sensor yang dipilih
	 */
	private void setValue(String com, String isiBaudRate, String isiDataBit, String isiParity, String isiStopBit,
			String isiFlowControl) {

		// menampilkan serial port
		serialPort.setBounds(141, 62, 139, 20);
		panel.add(serialPort);
		serialPort.setText(com);

		// menampilkan baudRate
		baudRate.setBounds(141, 92, 139, 20);
		panel.add(baudRate);
		baudRate.setText(isiBaudRate);

		// menampilkan data bit
		dataBits.setBounds(141, 123, 139, 20);
		panel.add(dataBits);
		dataBits.setText(isiDataBit);

		// menampilkan parity
		parityBit.setBounds(141, 154, 139, 20);
		panel.add(parityBit);
		parityBit.setText(isiParity);

		// menampilkan stop bit
		stopBit.setBounds(141, 185, 139, 20);
		panel.add(stopBit);
		stopBit.setText(isiStopBit);

		// menampilkan flow control
		flowControl.setBounds(141, 216, 139, 20);
		panel.add(flowControl);
		flowControl.setText(isiFlowControl);

	}

	/**
	 * Method untuk menambahkan list port name kedalam checkbox
	 */
	public void addToListSerialPort(String[] list) {
		this.print("Berikut adalah list port yang terhubung dengan device yang digunakan: ");

		for (String serialPort : list) {
			portListed.addItem(serialPort);
			this.print(serialPort);
		}
	}

	/**
	 * Method untuk hanldle aksi saat connect button di klik saat di klik akan
	 * membuka/menutup koneksi dengan sensor yang dipilih dan menampilkan spek dari
	 * sensor
	 * 
	 * @throws Exception
	 */
	public void connectButtonPress() throws Exception {
		this.controller.setChosenPort(this.portListed.getSelectedIndex()); // portChosen
		msg = "Sedang mencoba menghubungkan sensor \n" + this.controller.getDescriptivePortName()
				+ " dengan Basestation\n";
		this.print(msg);
		log.append(msg);
		if (this.controller.getDescriptivePortName().contains("Preon32")) {
			boolean status = this.controller.connect(this.portListed.getSelectedIndex()); // portChosen
			if (status) {
				msg = "Berhasil connect ke basestation\n" + "Size Flash Memory: " + this.controller.getSize() + "\n"
						+ "Waktu Preon32 Saat ini: " + this.controller.getTimePreon32() + "\n";
				String[] res = this.controller.value();
				this.setValue(res[0], res[1], res[2], res[3], res[4], res[5]);
				this.portListed.setEnabled(false);
				this.checkComponentButton.setEnabled(true);
				this.checkComponentButton.setEnabled(true);
				this.uploadButton.setEnabled(true);
				this.downloadButton.setEnabled(true);
				this.restartButton.setEnabled(true);
				this.sensorGetaranButton.setEnabled(true);
				this.sensorSuhuButton.setEnabled(true);
				this.sensorKelembabanUdaraButton.setEnabled(true);
				this.sensorTekananUdaraButton.setEnabled(true);
				connectButton.setText("Disconnect");
			} else {
				msg = "Sensor " + this.controller.getDescriptivePortName() + " berhasil terputus\n";
//				this.uploadButton.setEnabled(false);
				log.setText(null);
				setValue("", "0", "0", "0", "0", "0");
				this.portListed.setEnabled(true);
				this.checkComponentButton.setEnabled(false);
				this.uploadButton.setEnabled(false);
				this.downloadButton.setEnabled(false);
				this.restartButton.setEnabled(false);
				this.sensorGetaranButton.setEnabled(false);
				this.sensorSuhuButton.setEnabled(false);
				this.sensorKelembabanUdaraButton.setEnabled(false);
				this.sensorTekananUdaraButton.setEnabled(false);
				connectButton.setText("Connect");
			}
		} else {
			msg = "Program tidak bisa menghubungkan dengan " + this.controller.getDescriptivePortName()
					+ " karena bukan sensor Preon32\n";
		}
		this.print(msg);
		log.append(msg);

	}

	/**
	 * Method untuk handle aksi saat check component button di klik hanya bisa di
	 * klik saat sudah terkoneksi dengan sensor
	 * 
	 * @throws InterruptedException
	 */
	public void checkComponentButtonPress() throws InterruptedException {
		String msg = "";
		msg = "Sensor sedang melakukan check Component\n";
		this.print(msg);
		log.append(msg);
		this.controller.checkComponent();
		Thread.sleep(6000);
		msg = this.controller.getMessage();
		this.print(msg);
		log.append(msg);
	}

	/**
	 * Method untuk handle aksi saat restart button di klik hanya bisa di klik saat
	 * sudah terkoneksi dengan sensor
	 * 
	 */
	public void restartButtonPress() {
		msg = "Sensor dalam proses restart";
		this.print(msg);
		log.append(msg);
		if (this.controller.getDataConnection().isConnected()) {
			log.setText(null);
		}
		this.controller.restart(this.portListed.getSelectedIndex());
		msg = "Sensor berhasil di restart \n" + "Size Flash Memory: " + this.controller.getSize() + "\n"
				+ "Waktu Preon32 Saat ini: " + this.controller.getTimePreon32() + "\n";

		log.append(msg);
		log.append("\n");

	}

	/**
	 * Method untuk handle aksi saat upload button di klik hanya bisa di klik saat
	 * sudah terkoneksi dengan sensor
	 * 
	 * @throws IOException
	 */
	public void uploadButtonPress() throws IOException {
		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(".")); // sets current directory
			int response = fileChooser.showOpenDialog(null); // select file to open

			if (response == JFileChooser.APPROVE_OPTION) {
				File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
				if (file.getName().contains(".txt") || file.getName().contains(".java")) {
					this.controller.upload(file);

					msg = this.controller.getMessage();
					this.print(msg);
					log.append(msg);
					log.append("\n");
				} else {
					msg = "Hanya bisa upload file dengan format .txt atau .java \n";
					this.print(msg);
					log.append(msg);
					log.append("\n");
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void downloadButtonPress() throws IOException {
		try {
			msg = "Sensor sedang membaca data dari flash.\n";
			this.print(msg);
			log.append(msg);
			this.controller.download();
			msg = this.controller.getMessage();
			this.print(msg);
			log.append(msg);
			log.append("\n");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void sensorButtonPress(int input) {
		try {
			String msg = "";
			if (input == 5) {
				msg = "Preon32 akan mengecek suhu.\n";
			} else if (input == 6) {
				msg = "Preon32 akan mengecek tekanan udara.\n";
			} else if (input == 7) {
				msg = "Preon32 akan mengecek kelembaban udara.\n";
			} else if (input == 8) {
				msg = "Preon32 akan mengecek getaran.\n";
			}
			this.print(msg);
			log.append(msg);
			this.controller.sensor(input);

			msg = this.controller.getMessage();
			this.print(msg);
			log.append(msg);
			log.append("\n");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method untuk print sebuah string, digunakan saat proses debug
	 * 
	 * @param s adalah string yang akan ditampilakn
	 */
	public void print(String s) {
		System.out.println(s);
	}
}
