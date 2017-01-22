public class HelloWorld {
	static public void main(String[] args) {
		//System.err.println("Hello dll:"  +  "sdfsdf");
		System.out.println("Hello World before loading dynamic library!");
		System.load("/Users/simon/Documents/workspace_mars/jtransc/jtransc-main-run/example-gradle/HelloWorld.dylib");
		System.out.println("Hello World after loading dynamic library!");
		System.out.println(dooFoo(1, 3));
		System.out.println(dooFoo(2, 4));
		System.out.println(dooFoo(3, 5));
		System.out.println(dooFooo(new boolean[]{true, false, true, true}));
		System.out.println(dooFoo(4, 6));
		//System.out.println(dooFoor(4, 6, 4, 4));

	}

	public static native int dooFooo(boolean[] t);

	public static native int dooFoo(int t, int m);
	/*
	return t + m;
	* */

	public static native int dooFoor(int t, int m, long te, int s);
}