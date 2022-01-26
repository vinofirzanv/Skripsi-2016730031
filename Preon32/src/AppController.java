import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.fazecast.jSerialComm.SerialPort;
import com.virtenio.commander.io.DataConnection;
import com.virtenio.commander.io.SerialPortSpec;
import com.virtenio.commander.toolsets.preon32.Preon32Helper;

public class AppController {

	private static DataConnection dataConnection;
	private static BufferedInputStream in;
	private ArrayList<SerialPort> listSerialPorts; // dibuat arrayList karena pada awalnya contoh ada 5 yang mengandung preon hanya 3
	// jadi secara memori mengurangi karena arraylist bersifat dinamis
	private SerialPort chosenPort;
	private static int input, size;
	private static String message, waktuPreon32;
	public String waktuKomputer;
	public AppController() {
		try {
			// untuk mengambil list berupa object
			SerialPort[] arrSerialPort = SerialPort.getCommPorts();
			// masukin ke list
			this.listSerialPorts = this.filterPort(arrSerialPort, "Preon32"); // ada terisi seluru com yang preon32
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method untuk hanldle aksi saat connect button di klik saat di klik akan
	 * membuka/menutup koneksi dengan sensor yang dipilih dan menampilkan spek dari
	 * sensor kalau false ga connect kalau true connect
	 * 
	 * @throws Exception
	 */
	public boolean connect(int idx) throws Exception {
		boolean status = false;
		this.chosenPort = this.listSerialPorts.get(idx);

		if (dataConnection == null || !dataConnection.isConnected()) {
			try {
				init();
				Thread.sleep(3000);

				input = 1;
				dataConnection.write(input);
				dataConnection.flush();
				Thread.sleep(10200);
				
				write(this.getTimeComputer().length());
				byte[] waktu = waktuKomputer.getBytes("UTF-8");
				dataConnection.write(waktu);
				dataConnection.flush();
				Thread.sleep(2000);

				String m = read(in);
				size = Integer.parseInt(m);
				Thread.sleep(4000);

				String w = read(in);
				waktuPreon32 = w+"\n";
				Thread.sleep(4000);
				status = true;
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			try {
				dataConnection.close();

			} catch (IOException e) {
				e.printStackTrace();
			} 
		}

		input = 0;
		return status;
	}

	/**
	 * Method untuk handle aksi saat restart button di klik hanya bisa di klik saat
	 * sudah terkoneksi dengan sensor
	 * 
	 */
	public boolean restart(int idx) {
		if (dataConnection.isConnected()) {
			try {
				dataConnection.close();
				return this.connect(idx);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void upload(File file) throws IOException, InterruptedException {
		if (input == 0) {
			if (file.length() == 0) {
				message = "File " + file.getName() + " tidak dapat di upload karena kosong.\n";
			} else {
				print("Upload file");
				String msg1 = "";
				@SuppressWarnings("resource")
				BufferedReader br = new BufferedReader(new FileReader(file));
				String st = "";
				while ((st = br.readLine()) != null) {
					msg1 += st+"\n";
				}
				byte[] buffer = msg1.getBytes("UTF-8");
				System.out.println("Ukuran buffer.length : " + buffer.length);
				if (size >= buffer.length) {

					int length = 5120;
					int batas = 1;
					if (buffer.length < length) {
						length = buffer.length;
					} else {
						batas = (int) Math.floor(buffer.length / length) + 1;
					}

					System.out.println("Batas: " + batas);

					input = 2;
					try {
						write(input);

						String b = batas + "";
						write(b.length());
						byte[] ba = b.getBytes("UTF-8");
						// kirim byte dari batas
						dataConnection.write(ba);
						dataConnection.flush();

						Thread.sleep(100);
						int count = 0; // yang udah kekirim

						int s = buffer.length; // yang belum kekirim (sisa)

						for (int i = 0; i < batas; i++) {
							int len = count + length;

							if (len > buffer.length) {
								len = buffer.length;
								length = s;
							} else {
								s -= length;
							}

							System.out.println("S: " + s + " Length: " + length + " pengiriman ke-" + (i + 1));
							// send length
							String l = length + "";
							// kirim panjang string
							write(l.length());
							byte[] le = l.getBytes("UTF-8");
							// kirim byte dari length
							dataConnection.write(le);
							dataConnection.flush();

							// penampung data untuk dikirim
							byte[] temp = new byte[length];
							// pengingat batas akhir file yang perlu dikirim setiap pengiriman
							for (int j = count, k = 0; j < len; j++, k++) {
								temp[k] = buffer[j];
							}
							count += length;

							dataConnection.write(temp);
							dataConnection.flush();
							Thread.sleep(100);
						}

						size -= buffer.length;
						System.out.println("Size saat ini: " + size);
						message = "File " + file.getName() + " berhasil diupload.\n" + "Size saat ini: " + size + "\n";
					} catch (IOException e) {
						e.printStackTrace();
					}

				} else if (size == 0) {
					message = "Memori Penuh.\n";
				} else {
					message = "Ukuran file terlalu besar.\n";
				}

			}
			input = 0;
		}
	}

	public void download() throws IOException, InterruptedException {
		if (input == 0) {
			input = 3;
			write(input);

			int s = 1048576 - size;
			System.out.println(s);
			int length = 5120;
			int batas = 1;
			if (s < length) {
				length = s;
			} else {
				batas = (int) Math.floor(s / length) + 1;
			}
			System.out.println("Batas: " + " " + batas);

			String msg = "";
			if (s != 0) {
				for (int i = 0; i <= batas; i++) {
					System.out.println("Terima pengiriman ke-" + (i + 1));
					String temp = read(in);
					msg += temp;
					Thread.sleep(batas*400);
				}
			}

			if (msg == null || msg == "") {
				message = "Tidak ada data didalam flash.\n";
			} else {
				Date date = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
				FileWriter myWriter = new FileWriter(dateFormat.format(date) + ".txt");
				myWriter.write(msg);
				myWriter.close();
				message = "File berhasil di download kedalam projek Preon32 \n" + "dengan nama File: "
						+ dateFormat.format(date) + ".\n" + "Size saat ini: " + size + "\n";
			}
			input = 0;
		}

	}

	/**
	 * Method untuk handle aksi saat check component button di klik hanya bisa di
	 * klik saat sudah terkoneksi dengan sensor
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void checkComponent() {
		if (input == 0) {
			try {
				input = 4;
				write(input);
				Thread.sleep(6000);
				message = read(in);
				input = 0;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void sensor(int code) throws IOException, InterruptedException {
		if (input == 0) {
			input = code;
			write(input);
			Thread.sleep(3000 * 5);
			message = read(in);
			input = 0;
		}
	}

	private static String read(BufferedInputStream in) throws IOException {
		String msg = "";
		int i = 0;
		// membaca yang di print dari sensor
		while (i != -1 && in.available() > 0) {
			i = in.read();
			dataConnection.flush();
			msg += (char) i;
		}
		return msg;
	}

	/*
	 * mengembalikan data2 yang dibutuhkan untuk ditampilkan di GUI
	 */
	public String[] value() {
		String[] res = new String[6];
		SerialPort chosenPort = this.getChosenPort();
		SerialPortSpec sp = new SerialPortSpec(chosenPort.getSystemPortName().toString());
		res[0] = chosenPort.getDescriptivePortName();
		res[1] = sp.getBaudrate() + "";
		res[2] = sp.getDataBits() + "";
		res[3] = sp.getParity() + "";
		res[4] = sp.getStopBits() + "";
		res[5] = sp.getFlowControl() + "";
		return res;
	}

	public void setChosenPort(int idx) {
		this.chosenPort = this.listSerialPorts.get(idx);
	}

	public String getDescriptivePortName() {
		return this.chosenPort.getDescriptivePortName();
	}

	public SerialPort getChosenPort() {
		return this.chosenPort;
	}

	public int getSize() {
		return size;
	}

	public String getTimePreon32() {
		return waktuPreon32;
	}

	public String getTimeComputer() {
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		waktuKomputer = df.format(date);
		return waktuKomputer;
	}
	
	//masih berupa kumpulan array berisi objek dari library serial Port
	private ArrayList<SerialPort> filterPort(SerialPort[] arr, String target) {
		ArrayList<SerialPort> res = new ArrayList<SerialPort>();
		for (SerialPort serialport : arr) {
			if (serialport.getDescriptivePortName().contains(target)) {
				res.add(serialport);
			}
		}
		return res;
	}

	/**
	 * 
	 * @return ArrayList of Port Name if descriptivePortName contain "Preon32"
	 * Menyimpan nama COM1,2,3 dari array of objek serial port
	 */
	public String[] getListPort() {
		int size = this.listSerialPorts.size();
		String[] res = new String[size];
		int i = 0;
		for (SerialPort serialPort : this.listSerialPorts) {
			res[i] = serialPort.getSystemPortName();
			i++;
		}
		return res;
	}

	public DataConnection getDataConnection() {
		return dataConnection;
	}

	public int getInput() {
		return input;
	}

	public String getMessage() {
		return message;
	}

	private void write(int input) throws IOException, InterruptedException {
		dataConnection.write(input);
		dataConnection.flush();
		Thread.sleep(3000);
	}

	public void init() throws Exception {
		Preon32Helper nodeHelper = new Preon32Helper(this.chosenPort.getSystemPortName().toString(), 115200); // connected
		dataConnection = nodeHelper.runModule("BaseStation"); // run program "BaseStation" di node
		in = new BufferedInputStream(dataConnection.getInputStream()); // buffer

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
