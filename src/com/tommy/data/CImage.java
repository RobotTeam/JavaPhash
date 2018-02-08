package com.tommy.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CImage {

	public byte[] data;
	public int width;
	public int height;
	public int size;

	private static final int BYTE_SIZE = 8;

	public CImage(String filepath) {
		File f = new File(filepath);
		try {
			BufferedImage image = ImageIO.read(f);
			width = image.getWidth();
			height = image.getHeight();
			int numPixels = width * height;
			int numComponents = image.getColorModel().getNumComponents();
			int maxPixel = 0;
			data = new byte[width * height];
			if (image.getColorModel().getComponentSize(0) == BYTE_SIZE) {
				// Components are byte sized
				int bufferSize = numPixels * numComponents;
				size = bufferSize;
				byte[] tempData;
				try {
					tempData = new byte[bufferSize];
				} catch (OutOfMemoryError e) {
					System.out
							.println("Died trying to allocate a buffer of size: "
									+ bufferSize
									+ ". Please increas heap size!!");
					throw e;
				}
				image.getRaster()
						.getDataElements(0, 0, width, height, tempData);

				if (numComponents == 1) {
					image.getRaster()
							.getDataElements(0, 0, width, height, data);
				} else if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
					int j = 0;
					for (int i = 0; i < bufferSize; i += numComponents) {
						float y0 = ((tempData[i + 2] & 0xFF) * 25
								+ (tempData[i + 1] & 0xFF) * 129 + (tempData[i] & 0xFF) * 66);
						int y = Math.round(y0 / 256.0f) + 16;
						if (y > 255)
							y = 255;
						if (y > maxPixel) {
							maxPixel = y;
						}
						this.data[j] = (byte) y;
						j++;
					}

				} else {
					throw new IllegalArgumentException(
							"Can't work with this type of byte image: "
									+ image.getType());
				}
			} else {
				throw new IllegalArgumentException(
						"Can't work with non-byte image buffers");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public CImage() {
		// TODO Auto-generated constructor stub
	}

	public CImage(int N, int D) {
		data = new byte[D * N];
		width = N;
		height = D;
		size = N * D;
	}

	public void blur() {
		double b1 = -1.23227429;
		double b2 = 0.379624963;
		double a0 = 0.121062264;
		double a1 = -0.0384676233;
		double a2 = 0.110714294;
		double a3 = -0.0459582582;
		double coefp = 0.560531139;
		double coefn = 0.439468890;

		// X direction
		double[] Y = new double[width];

		for (int y = 0; y < height; y++) {
			int ptrX = 0;
			int ptrY = 0;
			double yb = 0;
			double yp = 0;
			double xp = 0;
			ptrX = y * width;
			xp = data[ptrX] & 0xFF;
			yb = coefp * xp;
			yp = coefp * xp;

			for (int m = 0; m < width; m++) {
				double xc = data[ptrX] & 0xFF;
				ptrX++;
				double yc = a0 * xc + a1 * xp - b1 * yp - b2 * yb;
				Y[ptrY] = yc;
				ptrY++;
				xp = xc;
				yb = yp;
				yp = yc;
			}

			double xn = 0;
			double xa = 0;
			double yn = 0;
			double ya = 0;

			xn = data[ptrX - 1] & 0xFF;
			xa = xn;
			yn = coefn * xn;
			ya = yn;

			for (int n = width - 1; n >= 0; n--) {
				ptrX--;
				double xc = data[ptrX] & 0xFF;
				;
				double yc = a2 * xn + a3 * xa - b1 * yn - b2 * ya;
				xa = xn;
				xn = xc;
				ya = yn;
				yn = yc;
				ptrY--;
				data[ptrX] = (byte) (Y[ptrY] + yc);
			}

		}

		// Y direction

		Y = new double[height];

		for (int x = 0; x < width; x++) {

			int ptrX = 0;
			int ptrY = 0;
			double yb = 0;
			double yp = 0;
			double xp = 0;
			ptrX = ptrX + x;
			xp = data[ptrX] & 0xFF;
			yb = coefp * xp;
			yp = coefp * xp;

			for (int m = 0; m < height; m++) {
				double xc = data[ptrX] & 0xFF;
				ptrX = ptrX + width;
				double yc = a0 * xc + a1 * xp - b1 * yp - b2 * yb;
				Y[ptrY] = yc;
				ptrY++;
				xp = xc;
				yb = yp;
				yp = yc;
			}

			double xn = 0;
			double xa = 0;
			double yn = 0;
			double ya = 0;

			xn = data[ptrX - width] & 0xFF;
			xa = xn;
			yn = coefn * xn;
			ya = yn;

			for (int n = height - 1; n >= 0; n--) {
				ptrX = ptrX - width;
				double xc = data[ptrX] & 0xFF;
				;
				double yc = a2 * xn + a3 * xa - b1 * yn - b2 * ya;
				xa = xn;
				xn = xc;
				ya = yn;
				yn = yc;
				ptrY--;
				data[ptrX] = (byte) (Y[ptrY] + yc);
			}

		}

	}

	public boolean equals(CImage cimage) {
		if (this.width != cimage.width)
			return false;
		if (this.height != cimage.height)
			return false;

		for (int i = 0; i < cimage.size; i++) {
			if (this.data[i] != cimage.data[i]) {
				return false;
			}
		}
		return true;
	}

	public void save(String path) {
		BufferedImage temp = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_GRAY);
		temp.getRaster().setDataElements(0, 0, width, height, data);
		try {
			ImageIO.write(temp, "jpg", new File(path));
		} catch (IOException e) {
		}
	}

	public void printData() {
		for (byte temp : data) {
			System.out.println("" + (temp & 0xFF));
		}
	}

	public static void main(String[] args) {
		CImage c = new CImage("D:\\cars\\temp\\1.jpg");
		c.blur();
		c.save("D:\\cars\\temp\\g2.jpg");
	}

}
