package p;
//renaming A.m to k
class A{
	static void m(){
	}
}
class test{
	void m(){
		class X extends A{
			void m(){
		}
	}
}