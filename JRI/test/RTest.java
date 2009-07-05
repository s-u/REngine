import org.rosuda.REngine.*;

class TestException extends Exception {
	public TestException(String msg) { super(msg); }
}

// This is the same test as in Rserve but it's using JRI instead

public class RTest {
	public static void main(String[] args) {
		try { 
			REngine eng = REngine.engineForClass("org.rosuda.REngine.JRI.JRIEngine");
			// alternatively you could use
			// REngine eng = new JRIEngine();
			// if you imported org.rosuda.REngine.JRI.JRIEngine
			// the above illustrates how you can switch the engine (JRI vs Rserve) without re-compiling your code
			
			System.out.println("R Version: " + eng.parseAndEval("R.version.string").asString());
			
			{
				System.out.println("* Test string and list retrieval");
				RList l = eng.parseAndEval("{d=data.frame(\"huhu\",c(11:20)); lapply(d,as.character)}").asList();
				int cols = l.size();
				int rows = l.at(0).length();
				String[][] s = new String[cols][];
				for (int i=0; i<cols; i++) s[i]=l.at(i).asStrings();
				System.out.println("PASSED");
			}
			
			{
				System.out.println("* Test NA/NaN support in double vectors...");
				double R_NA = Double.longBitsToDouble(0x7ff00000000007a2L);
				// int R_NA_int = -2147483648; // just for completeness
				double x[] = { 1.0, 0.5, R_NA, Double.NaN, 3.5 };
				eng.assign("x",x);
				String nas = eng.parseAndEval("paste(capture.output(print(x)),collapse='\\n')").asString();
				System.out.println(nas);
				if (!nas.equals("[1] 1.0 0.5  NA NaN 3.5"))
					throw new TestException("NA/NaN assign+retrieve test failed");
				System.out.println("PASSED");
			}
			
			{
				System.out.println("* Test assigning of lists and vectors ...");
				RList l = new RList();
				l.put("a",new REXPInteger(new int[] { 0,1,2,3}));
				l.put("b",new REXPDouble(new double[] { 0.5,1.2,2.3,3.0}));
				System.out.println("  assign x=pairlist");
				eng.assign("x", new REXPList(l));
				System.out.println("  assign y=vector");
				eng.assign("y", new REXPGenericVector(l));
				System.out.println("  assign z=data.frame");
				eng.assign("z", REXP.createDataFrame(l));
				System.out.println("  pull all three back to Java");
				REXP x = eng.parseAndEval("x");
				System.out.println("  x = "+x);
				x = eng.parseAndEval("y");
				System.out.println("  y = "+x);
				x = eng.parseAndEval("z");
				System.out.println("  z = "+x);
				System.out.println("PASSED");
			}
			
			{ // regression: object bit was not set for Java-side generated objects before 0.5-3
				System.out.println("* Testing functionality of assembled S3 objects ...");
				// we have already assigned the data.frame in previous test, so we jsut re-use it
				REXP x = eng.parseAndEval("z[2,2]");
				System.out.println("  z[2,2] = " + x);
				if (x == null || x.length() != 1 || x.asDouble() != 1.2)
					throw new TestException("S3 object bit regression test failed");
				System.out.println("PASSED");
			}
			
			{ // this test does a pull and push of a data frame. It will fail when the S3 test above failed.
				System.out.println("* Testing pass-though capability for data.frames ...");
				REXP df = eng.parseAndEval("{data(iris); iris}");
				eng.assign("df", df);
				REXP x = eng.parseAndEval("identical(df, iris)");
				System.out.println("  identical(df, iris) = "+x);
				if (x == null || !x.isLogical() || x.length() != 1 || !((REXPLogical)x).isTrue()[0])
					throw new TestException("Pass-through test for a data.frame failed");
				System.out.println("PASSED");
			}
			
            { // factors
                System.out.println("* Test support of factors");
                REXP f = eng.parseAndEval("factor(paste('F',as.integer(runif(20)*5),sep=''))");
				System.out.println("  f="+f);
                System.out.println("  isFactor: "+f.isFactor()+", asFactor: "+f.asFactor());
                if (!f.isFactor() || f.asFactor() == null) throw new TestException("factor test failed");
                System.out.println("  singe-level factor used to degenerate:");
                f = eng.parseAndEval("factor('foo')");
                System.out.println("  isFactor: "+f.isFactor()+", asFactor: "+f.asFactor());
                if (!f.isFactor() || f.asFactor() == null) throw new TestException("single factor test failed (not a factor)");
				if (!f.asFactor().at(0).equals("foo")) throw new TestException("single factor test failed (wrong value)");
                System.out.println("  test factors with null elements contents:");
				eng.assign("f", new REXPFactor(new RFactor(new String[] { "foo", "bar", "foo", "foo", null, "bar" })));
				f = eng.parseAndEval("f");
                if (!f.isFactor() || f.asFactor() == null) throw new TestException("factor assign-eval test failed (not a factor)");
				System.out.println("  f = "+f.asFactor());
				f = eng.parseAndEval("as.factor(c(1,'a','b',1,'b'))");
				System.out.println("  f = "+f);
                if (!f.isFactor() || f.asFactor() == null) throw new TestException("factor test failed (not a factor)");
				System.out.println("PASSED");
            }
			
			
			{
				System.out.println("* Lowess test");
				double x[] = eng.parseAndEval("rnorm(100)").asDoubles();
				double y[] = eng.parseAndEval("rnorm(100)").asDoubles();
				eng.assign("x", x);
				eng.assign("y", y);
				RList l = eng.parseAndEval("lowess(x,y)").asList();
				System.out.println("  "+l);
				x = l.at("x").asDoubles();
				y = l.at("y").asDoubles();
				System.out.println("PASSED");
			}
			
			{
				// multi-line expressions
				System.out.println("* Test multi-line expressions");
				if (eng.parseAndEval("{ a=1:10\nb=11:20\nmean(b-a) }\n").asInteger()!=10)
					throw new TestException("multi-line test failed.");
				System.out.println("PASSED");
			}
			{
				System.out.println("* Matrix tests\n  matrix: create a matrix");
				int m=100, n=100;
				double[] mat=new double[m*n];
				int i=0;
				while (i<m*n) mat[i++]=i/100;
				System.out.println("  matrix: assign a matrix");
				eng.assign("m", mat);
				eng.parseAndEval("m<-matrix(m,"+m+","+n+")", null, false); // don't return the result - it has a similar effect to voidEval in Rserve
				System.out.println("matrix: cross-product");
				double[][] mr=eng.parseAndEval("crossprod(m,m)").asDoubleMatrix();
				System.out.println("PASSED");
			}
			
			{
				System.out.println("* Test serialization and raw vectors");
				byte[] b = eng.parseAndEval("serialize(ls, NULL, ascii=FALSE)").asBytes();
				System.out.println("  serialized ls is "+b.length+" bytes long");
				eng.assign("r", new REXPRaw(b));
				String[] s = eng.parseAndEval("unserialize(r)()").asStrings();
				System.out.println("  we have "+s.length+" items in the workspace");
				System.out.println("PASSED");
			}
			
			{
				System.out.println("* Test enviroment support");
				REXP x = eng.parseAndEval("new.env(parent=baseenv())");
				System.out.println("  new.env() = " + x);
				if (x == null || !x.isEnvironment()) throw new TestException("pull of an environemnt failed");
				REXPEnvironment e = (REXPEnvironment) x;
				e.assign("foo", new REXPString("bar"));
				x = e.get("foo");
				System.out.println("  get(\"foo\") = " + x);
				if (x == null || !x.isString() || !x.asString().equals("bar")) throw new TestException("assign/get in an environemnt failed");
				x = eng.newEnvironment(e, true);
				System.out.println("  eng.newEnvironment() = " + x);
				if (x == null || !x.isEnvironment()) throw new TestException("Java-side environment creation failed");
				x = ((REXPEnvironment)x).parent(true);
				System.out.println("  parent = " + x);
				if (x == null || !x.isEnvironment()) throw new TestException("parent environment pull failed");
				x = e.get("foo");
				System.out.println("  get(\"foo\",parent) = " + x);
				if (x == null || !x.isString() || !x.asString().equals("bar")) throw new TestException("get in the parent environemnt failed");
				System.out.println("PASSED");
			}
			
			/* setEncoding is Rserve's extension - with JRI you have to use UTF-8 locale so this test will fail unless run in UTF-8
			{ // string encoding test (will work with Rserve 0.5-3 and higher only)
				System.out.println("* Test string encoding support ...");
				String t = "ひらがな"; // hiragana (literally, in hiragana ;))
				eng.setStringEncoding("utf8"); 
				// -- Just in case the console is not UTF-8 don't display it
				//System.out.println("  unicode text: "+t);
				eng.assign("s", t);
				REXP x = eng.parseAndEval("nchar(s)");
				System.out.println("  nchar = " + x);
				if (x == null || !x.isInteger() || x.asInteger() != 4)
					throw new TestException("UTF-8 encoding string length test failed");
				// we cannot really test any other encoding ..
				System.out.println("PASSED");
			} */
			
			eng.close(); // close the engine connection
			
			System.out.println("Done.");
			
		} catch (REXPMismatchException me) {
			// some type is different from what you (the programmer) expected
			System.err.println("Type mismatch: "+me);
			me.printStackTrace();
			System.exit(1);
		} catch (REngineException ee) {
			// something went wring in the engine
			System.err.println("REngine exception: "+ee);
			ee.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException cnfe) {
			// class not found is thrown by engineForClass
			System.err.println("Cannot find JRIEngine class - please fix your class path!\n"+cnfe);
			System.exit(1);
		} catch (Exception e) {
			// some other exception ...
			System.err.println("Exception: "+e);
			e.printStackTrace();
			System.exit(1);
		}
	}
}
