One JAVA version of PHash (Perceptual Hash) algorithm (JAVA 版本的 PHash算法)
100%源自于C++ 的PHash (Perceptual Hash) algorithm 感知哈希算法
官网：https://www.phash.org/
可以用于比较两张图片的相似程度
You can get the similarity between two images using this algorithm.

主函数入口：

com.tommy.main.Main

public class Main {
	public static void main(String[] args) {
		System.out.println("hello");
		Phash phash = new Phash();
		CImage imA=new CImage("D:\\cars\\temp\\3.jpg");// replace this with your own image path
		CImage imB=new CImage("D:\\cars\\temp\\4.jpg");// 替换成你自己的图片路径
		phash._ph_compare_images(imA, imB);
	}
}
