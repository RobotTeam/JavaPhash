package com.tommy.main;

import com.tommy.algorithm.Phash;
import com.tommy.data.CImage;

public class Main {

	public static void main(String[] args) {
		System.out.println("hello");
		Phash phash = new Phash();
		CImage imA=new CImage("D:\\cars\\temp\\3.jpg");
		CImage imB=new CImage("D:\\cars\\temp\\4.jpg");
		phash._ph_compare_images(imA, imB);
	}
}
