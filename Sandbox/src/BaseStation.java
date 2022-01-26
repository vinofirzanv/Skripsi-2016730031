import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.virtenio.driver.adc.ADCException;
import com.virtenio.driver.adc.NativeADC;
import com.virtenio.driver.device.ADT7410;
import com.virtenio.driver.device.ADXL345;
import com.virtenio.driver.device.MPL115A2;
import com.virtenio.driver.device.SHT21;
import com.virtenio.driver.flash.Flash;
import com.virtenio.driver.flash.FlashException;
import com.virtenio.driver.gpio.GPIO;
import com.virtenio.driver.gpio.GPIOException;
import com.virtenio.driver.gpio.NativeGPIO;
import com.virtenio.driver.i2c.I2C;
import com.virtenio.driver.i2c.I2CException;
import com.virtenio.driver.i2c.NativeI2C;
import com.virtenio.driver.irq.IRQException;
import com.virtenio.driver.ram.NativeRAM;
import com.virtenio.driver.ram.RAMException;
import com.virtenio.driver.spi.NativeSPI;
import com.virtenio.driver.spi.SPIException;
import com.virtenio.driver.usart.NativeUSART;
import com.virtenio.driver.usart.USART;
import com.virtenio.driver.usart.USARTException;
import com.virtenio.driver.usart.USARTParams;
import com.virtenio.preon32.examples.common.USARTConstants;
import com.virtenio.preon32.node.Node;
import com.virtenio.radio.RadioDriverException;

public class BaseStation {
	private static USART usart;
	private static Flash flash;
	private static int sizeFlash, address, input;
	private static OutputStream outStream, out;
	private static InputStream inStream;

	public static void main(String[] args) throws Exception {
		useUSART();
		new Thread() {
			public void run() {
				try {
					receive();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	//sensor harus tetap standby untuk menerima input dari komputer
	public static void receive() {
		new Thread() {
			public void run() {
				while (usart != null) {
					try {
						input = usart.read();
						if (input == 1) {
							connect();
						} else if (input == 2) {
							uploadData();
						} else if (input == 3) {
							downloadData();
						} else if (input == 4) {
							getComponent();
						} else if (input == 5) {
							sensorSuhu();
						} else if (input == 6) {
							sensorTekananUdara();
						} else if (input == 7) {
							sensorKelembabanUdara();
						} else if (input == 8) {
							sensorGetaran();
						}
				}
						catch (Exception e) {
					}
				}
			};
		}.start();
	}


	private static void connect() throws Exception {
		flash = Node.getInstance().getFlash();
		flash.open();
		eraseChip();
		sizeFlash = flash.getChipSize();
		flash.close();
		address = 0;

		String msg = sizeFlash+"";
		byte[] res = msg.getBytes("UTF-8");
		write(res);
		Thread.sleep(2000);

		int waktu = usart.read();
		byte[] waktuKomputer = read(waktu);
		Thread.sleep(4000);

		write(waktuKomputer);
		Thread.sleep(4000);
	}

	private static void eraseChip() throws FlashException {
		flash.eraseChip();
		flash.waitWhileBusy();
	}

	/**
	 * Method untuk meng-upload data berupa text ke dalam memory flash
	 * 
	 * @throws Exception
	 */
	private static void uploadData() throws Exception {
		flash.open();

		outStream = flash.getOutputStream(address);

		int b = usart.read();
		byte[] ba = read(b);
		int batas = Integer.parseInt(new String(ba));
		Thread.sleep(100);

		for (int i = 0; i < batas; i++) {
			// baca panjang string untuk length
			int l = usart.read(); // panjang string length
			// baca byte dari length
			byte[] le = read(l); // baca string length
			// convert string ke int untuk length
			int length = Integer.parseInt(new String(le));

			byte[] in = read(length);

			// Write With No Erase
			outStream.write(in);

			sizeFlash -= length;
			address += length;
			Thread.sleep(100);
		}
		flash.close();
		
	}

	/*
	 * Method yang digunakan untuk membaca data dari memory flash dari awal sampai
	 * akhir
	 * Berdasarkan experimen yang saya lakukan, jika thread.sleep terlalu kecil maka pada saat
	 * penulisan ke dalam file akan ada string yang terpotong
	 */
	private static void downloadData() throws Exception {
		if (address != 0) {
			flash.open();
			int size = flash.getChipSize() - sizeFlash;
			int length = 5120;
			int batas = 0;
			if (size < length) {
				length = size;
				batas = 1;
			} else {
				batas = (int) Math.floor(size / length) + 1;
			}

			int count = 0; // yang udah kekirim
			int s = size; // yang belum kekirim

			for (int i = 0; i <= batas; i++) {
				if ((count + length) > size) {
					length = s;
				}
				inStream = flash.getInputStream(count);
				byte[] res = new byte[length];
				inStream.read(res);

				count += length;
				s -= length;
				if (s < 0) {
					s = 0;
				}
				write(res);
				Thread.sleep(batas*400);

			}
			flash.close();
		} else {
			out.write(0);
			usart.flush();
		}

	}

	// Method untuk mendapatkan Komponen Preon32
	public static void getComponent() throws Exception {

		String msg = "";
		msg = "Komponen Preon32 yang tersedia : " + "\n" + BaseStation.getRAM() + BaseStation.getChannel()
				+ BaseStation.getMemoryFlash() + BaseStation.getSizeFlashAvailable()+"\n";
		byte[] res = msg.getBytes("UTF-8");
		write(res);

	}

	// Method untuk mendapatkan Kapasitas Memori Flash
	public static String getMemoryFlash() throws FlashException, SPIException, GPIOException {
		BaseStation.flash.open();
		String msg = "Technology : Serial Flash" + "\n" + "Size Awal : " + BaseStation.flash.getChipSize() + " Byte"
				+ "\n";
		BaseStation.flash.close();
		return msg;
	}

	public static String getSizeFlashAvailable() {
		String msg = "Size Akhir Saat ini : " + BaseStation.sizeFlash + " Byte" + "\n";
		return msg;
	}

	public static String getRAM() throws RAMException {
		NativeRAM ram = NativeRAM.getInstance(0);
		String msg = "Processor core :  32-Bit-RISC" + "\n" + "RAM : " + ram.getSize() + " kByte SRAM" + "\n"
				+ "ROM : 256 kByte Flash" + "\n";
		return msg;
	}

	public static String getChannel() throws ADCException, SPIException, IRQException, RadioDriverException {
		NativeADC adc = NativeADC.getInstance(0);
		String msg = "Channels : " + adc.getChannelCount() + "\n" + "Channel Spacing : 5" + "\n";
		return msg;
	}

	/**
	 * Method untuk membaca tiap baris yang dikirimkan oleh program
	 * 
	 * @param length panjang kalimat
	 * @return array of byte, hasil convert string ke array of bytes yang diterima
	 * @throws USARTException
	 */
	private static byte[] read(int length) throws USARTException, Exception {
		byte[] res = new byte[length];
		for (int i = 0; i < length; i++) {
			res[i] = (byte) usart.read();
		}

		return res;
	}

	private static void write(byte[] res) throws USARTException, IOException {
		out = usart.getOutputStream();
		out.write(res);
		out.flush();
		out.close();
	}

	public static void sensorSuhu()
			throws I2CException, InterruptedException, IOException, USARTException, GPIOException {
		// Inisialisasi I2C untuk bus
		NativeI2C i2c = NativeI2C.getInstance(1);
		i2c.open(I2C.DATA_RATE_400);

		// inisialisasi modul untuk sensor suhu
		ADT7410 temperatureSensor = new ADT7410(i2c, ADT7410.ADDR_0, null, null);
		temperatureSensor.open();
		temperatureSensor.setMode(ADT7410.CONFIG_MODE_CONTINUOUS);// set continuous mode

		String msg = "";
		for (int i = 0; i < 5; i++) {
			float celsius = temperatureSensor.getTemperatureCelsius();// membaca suhu
			String suhu = Float.toString(celsius);
			msg += "Suhu: " + suhu.substring(0, 7) + "[C]\n";
			Thread.sleep(3000);
		}
		i2c.close();

		byte[] res = msg.getBytes("UTF-8");
		write(res);

	}

	public static void sensorTekananUdara()
			throws I2CException, GPIOException, InterruptedException, USARTException, IOException {

//		System.out.println("Inisialisasi Native I2C");
		NativeI2C i2c = NativeI2C.getInstance(1);
		i2c.open(I2C.DATA_RATE_400);

//		System.out.println("Inisialisasi GPIO");
		GPIO resetPin = NativeGPIO.getInstance(24);
		GPIO shutDownPin = NativeGPIO.getInstance(12);

//		System.out.println("Inisialisasi Sensor MPL115A2 (Tekanan Udara)");
		MPL115A2 pressureSensor = new MPL115A2(i2c, resetPin, shutDownPin);
		pressureSensor.open();
		pressureSensor.setReset(false);
		pressureSensor.setShutdown(false);

		String msg = "";
		for (int i = 0; i < 5; i++) {
			pressureSensor.startBothConversion();
			Thread.sleep(MPL115A2.BOTH_CONVERSION_TIME);
			int pressurePr = pressureSensor.getPressureRaw();
			int tempRaw = pressureSensor.getTemperatureRaw();
			float pressure = pressureSensor.compensate(pressurePr, tempRaw);
			String tekanan = Float.toString(pressure);
			msg += "Tekanan Udara: " + tekanan.substring(0, 7) + "kPa\n";

			Thread.sleep(3000);
		}
		i2c.close();
		pressureSensor.close();

		byte[] res = msg.getBytes("UTF-8");
		write(res);
	}

	public static void sensorKelembabanUdara() throws Exception {
//		System.out.println("I2C(Init)");
		NativeI2C i2c = NativeI2C.getInstance(1);
		i2c.open(I2C.DATA_RATE_400);

//		System.out.println("SHT21(Init)");
		SHT21 sht21 = new SHT21(i2c);
		sht21.open();
		sht21.setResolution(SHT21.RESOLUTION_RH12_T14);
		sht21.reset();

		String msg = "";
		for (int i = 0; i < 5; i++) {
			// humidity conversion
			Thread.sleep(MPL115A2.BOTH_CONVERSION_TIME);
			sht21.startRelativeHumidityConversion();
			Thread.sleep(100);
			int rawRH = sht21.getRelativeHumidityRaw();
			float rh = SHT21.convertRawRHToRHw(rawRH);
			String kelembaban = Float.toString(rh);
			msg += "Kelembaban Udara : " + kelembaban.substring(0, 7) + "%RH\n";
			Thread.sleep(3000);

		}
		i2c.close();
		sht21.close();

		byte[] res = msg.getBytes("UTF-8");
		write(res);

	}

	private static void sensorGetaran()
			throws SPIException, GPIOException, InterruptedException, USARTException, IOException {

//		System.out.println("Inisialiasasi GPIO");
		GPIO accelCs = NativeGPIO.getInstance(20);

//		System.out.println("Inisialisasi SPI");
		NativeSPI spi = NativeSPI.getInstance(0);
		spi.open(ADXL345.SPI_MODE, ADXL345.SPI_BIT_ORDER, ADXL345.SPI_MAX_SPEED);

//		System.out.println("Inisialisasi Sensor ADXL345 (Getaran)");
		ADXL345 accelerationSensor = new ADXL345(spi, accelCs);
		accelerationSensor.open();
		accelerationSensor.setDataFormat(ADXL345.DATA_FORMAT_RANGE_2G);
		accelerationSensor.setDataRate(ADXL345.DATA_RATE_3200HZ);
		accelerationSensor.setPowerControl(ADXL345.POWER_CONTROL_MEASURE);

		short[] values = new short[3];
		String msg = "";
		for (int i = 0; i < 5; i++) {
			try {
				accelerationSensor.getValuesRaw(values, 0);
				msg += "Getaran : " + Arrays.toString(values) + "g\n";
			} catch (Exception e) {
				System.out.println("ADXL345 error");
			}
			Thread.sleep(3000);
		}
		spi.close();
		accelerationSensor.close();

		byte[] res = msg.getBytes("UTF-8");
		write(res);

	}

	private static USART configUSART() {
		int instanceID = 0;
		USARTParams params = USARTConstants.PARAMS_115200;
		NativeUSART usart = NativeUSART.getInstance(instanceID);
		try {
			usart.close();
			usart.open(params);
			return usart;
		} catch (Exception e) {
			return null;
		}
	}

	private static void useUSART() throws Exception {
		usart = configUSART();
	}
}
