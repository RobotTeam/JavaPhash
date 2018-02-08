package com.tommy.algorithm;

import com.tommy.data.CImage;
import com.tommy.data.Digest;
import com.tommy.data.Features;
import com.tommy.data.Projections;

public class Phash {
	private static final double SQRT_TWO = Math.sqrt(2);
	private static final int UCHAR_MAX = 255;
	private static final double[] THETA_180;
	private static final double[] TAN_THETA_180;

	static {
		THETA_180 = new double[180];
		TAN_THETA_180 = new double[180];
		for (int i = 0; i < 180; i++) {
			THETA_180[i] = i * Math.PI / 180;
			TAN_THETA_180[i] = Math.tan(THETA_180[i]);
		}
	}

	public boolean ph_radon_projections(CImage img, int N, Projections projs) {
		int width = img.width;
		int height = img.height;
		int D = (width > height) ? width : height;
		int x_off = (width >> 1) + (width & 0x1);
		int y_off = (height >> 1) + (height & 0x1);
		projs.R = new CImage(N, D);
		projs.nb_pix_perline = new int[N];
		projs.size = N;

		for (int i = 0; i < projs.nb_pix_perline.length; i++) {
			projs.nb_pix_perline[i] = 0;
		}

		CImage ptr_radon_map = projs.R;
		int[] nb_per_line = projs.nb_pix_perline;

		for (int k = 0; k < N / 4 + 1; k++) {
			double alpha = TAN_THETA_180[k];
			for (int x = 0; x < D; x++) {
				double y = alpha * (x - x_off);
				int yd = (int) Math.floor(y + (y >= 0 ? 0.5 : -0.5));
				if ((yd + y_off >= 0) && (yd + y_off < height) && (x < width)) {
					ptr_radon_map.data[k + x * N] = img.data[x
							+ ((yd + y_off) * width)];
					nb_per_line[k]++;
				}

				if ((yd + x_off >= 0) && (yd + x_off < width) && (k != N / 4)
						&& (x < height)) {
					ptr_radon_map.data[(N / 2 - k) + x * N] = img.data[(yd + x_off)
							+ x * width];
					nb_per_line[N / 2 - k]++;
				}
			}
		}

		int j = 0;
		for (int k = 3 * N / 4; k < N; k++) {
			double alpha = TAN_THETA_180[k];
			for (int x = 0; x < D; x++) {
				double y = alpha * (x - x_off);
				int yd = (int) Math.floor(y + (y >= 0 ? 0.5 : -0.5));
				if ((yd + y_off >= 0) && (yd + y_off < height) && (x < width)) {
					ptr_radon_map.data[k + x * N] = img.data[x
							+ ((yd + y_off) * width)];
					nb_per_line[k]++;
				}

				if ((y_off - yd >= 0) && (y_off - yd < width)
						&& (2 * y_off - x >= 0) && (2 * y_off - x < height)
						&& (k != 3 * N / 4)) {
					ptr_radon_map.data[(k - j) + x * N] = img.data[(-yd + y_off)
							+ (-(x - y_off) + y_off) * width];
					nb_per_line[k - j]++;
				}

			}

			j = j + 2;

		}

		return true;

	}

	public boolean ph_feature_vector(Projections projs, Features fv) {
		CImage projection_map = projs.R;
		int[] nb_perline = projs.nb_pix_perline;
		int N = projs.size;
		int D = projection_map.height;
		fv.features = new double[N];

		for (int i = 0; i < fv.features.length; i++) {
			fv.features[i] = 0;
		}

		fv.size = N;

		double[] feat_v = fv.features;
		double sum = 0.0;
		double sum_sqd = 0.0;

		for (int k = 0; k < N; k++) {
			double line_sum = 0.0;
			double line_sum_sqd = 0.0;
			int nb_pixels = nb_perline[k];
			for (int i = 0; i < D; i++) {
				line_sum += projection_map.data[k + (i * projection_map.width)] & 0xFF;
				line_sum_sqd += (projection_map.data[k
						+ (i * projection_map.width)] & 0xFF)
						* (projection_map.data[k + (i * projection_map.width)] & 0xFF);
			}
			feat_v[k] = (line_sum_sqd / nb_pixels) - (line_sum * line_sum)
					/ (nb_pixels * nb_pixels);
			sum += feat_v[k];
			sum_sqd += feat_v[k] * feat_v[k];
		}

		double mean = sum / N;
		double var = Math.sqrt((sum_sqd / N) - (sum * sum) / (N * N));

		for (int i = 0; i < N; i++) {
			feat_v[i] = (feat_v[i] - mean) / var;
		}

		return true;
	}

	public boolean ph_dct(Features fv, Digest digest) {

		int N = fv.size;
		int nb_coeffs = 40;

		digest.coeffs = new int[nb_coeffs];
		digest.size = nb_coeffs;

		double[] R = fv.features;
		int[] D = digest.coeffs;

		double[] D_temp = new double[nb_coeffs];

		for (int i = 0; i < D_temp.length; i++) {
			D_temp[i] = 0;
		}

		double max = 0.0;
		double min = 0.0;

		for (int k = 0; k < nb_coeffs; k++) {
			double sum = 0.0;
			for (int n = 0; n < N; n++) {
				double temp = R[n]
						* Math.cos((Math.PI * (2 * n + 1) * k) / (2 * N));
				sum += temp;
			}
			if (k == 0)
				D_temp[k] = sum / Math.sqrt((double) N);
			else
				D_temp[k] = sum * SQRT_TWO / Math.sqrt((double) N);
			if (D_temp[k] > max)
				max = D_temp[k];
			if (D_temp[k] < min)
				min = D_temp[k];
		}

		for (int i = 0; i < nb_coeffs; i++) {
			D[i] = (int) (UCHAR_MAX * (D_temp[i] - min) / (max - min));
		}

		return true;

	}

	public boolean _ph_image_digest(CImage img, Digest digest, int N) {
		img.blur();
		Projections projs = new Projections();
		ph_radon_projections(img, N, projs);
		Features features = new Features();
		ph_feature_vector(projs, features);
		ph_dct(features, digest);
		return true;
	}

	public double ph_crosscorr(Digest x, Digest y) {

		int N = y.size;

		int[] x_coeffs = x.coeffs;
		int[] y_coeffs = y.coeffs;

		double[] r = new double[N];
		double sumx = 0.0;
		double sumy = 0.0;
		for (int i = 0; i < N; i++) {
			sumx += x_coeffs[i];
			sumy += y_coeffs[i];
		}

		double meanx = sumx / N;
		double meany = sumy / N;
		double max = 0;

		for (int d = 0; d < N; d++) {
			double num = 0.0;
			double denx = 0.0;
			double deny = 0.0;
			for (int i = 0; i < N; i++) {
				num += (x_coeffs[i] - meanx)
						* (y_coeffs[(N + i - d) % N] - meany);
				denx += Math.pow((x_coeffs[i] - meanx), 2);
				deny += Math.pow((y_coeffs[(N + i - d) % N] - meany), 2);
			}
			r[d] = num / Math.sqrt(denx * deny);
			if (r[d] > max)
				max = r[d];
		}

		return max;
	}

	public double _ph_compare_images(CImage imA, CImage imB) {
		int N = 180;

		Digest digestA = new Digest();
		_ph_image_digest(imA, digestA, N);

		Digest digestB = new Digest();
		_ph_image_digest(imB, digestB, N);

		double pcc = ph_crosscorr(digestA, digestB);

		return pcc;
	}

}
