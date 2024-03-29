/**
 * 
 */
package itjava.model;

import itjava.data.NodeToCompare;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;

/**
 * @author Aniket
 *
 */
public class Matrix {
	private ArrayList<CompilationUnitFacade> facadeList;
	private ArrayList<Similarity> sortedSimilarity;
	private float[][] similarity;
	private float UNTOUCHED = -999;
	private float ERROR = -666;

	private float[][] importSimilarity;
	private float[][] classInstanceSimilarity;
	private float[][] methodInvocationSimilarity;
	private float[][] variableDeclarationSimilarity;
	
	/**
	 * Creates a blank [][] array to store similarity corresponding to all the compilation units present 
	 * in the {@link ArrayList}<{@link CompilationUnitFacade}>. 
	 */
	public Matrix(ArrayList<CompilationUnitFacade> compilationUnitFacadeList) {
		facadeList = compilationUnitFacadeList;
		similarity = new float[facadeList.size()] [facadeList.size()];
		for (int i = 0; i < compilationUnitFacadeList.size(); i++) {
			java.util.Arrays.fill(similarity[i], UNTOUCHED);
		}
		importSimilarity = similarity.clone();
		classInstanceSimilarity = similarity.clone();
		methodInvocationSimilarity = similarity.clone();
		variableDeclarationSimilarity = similarity.clone();
		
		sortedSimilarity = new ArrayList<Matrix.Similarity>();
	}

	public boolean contains(CompilationUnitFacade y, CompilationUnitFacade x) {
		return (similarity[facadeList.indexOf(x)][facadeList.indexOf(y)] != UNTOUCHED || 
				similarity[facadeList.indexOf(y)][facadeList.indexOf(x)] != UNTOUCHED);
	}

	public void setValues(CompilationUnitFacade x, CompilationUnitFacade y) {
		int indexOfX = facadeList.indexOf(x);
		int indexOfY = facadeList.indexOf(y);
		float importVal = ERROR, classInstanceVal= ERROR, methodVal= ERROR, variableDecVal = ERROR;
		try {
			importVal = CalculateSimilarity(x.getTFVector().importDeclarationsTF.values().toArray(new TFIDF[0]), y.getTFVector().importDeclarationsTF.values().toArray(new TFIDF[0]));
			classInstanceVal = CalculateSimilarity(x.getTFVector().classInstancesTF.values().toArray(new TFIDF[0]), y.getTFVector().classInstancesTF.values().toArray(new TFIDF[0]));
			methodVal = CalculateSimilarity(x.getTFVector().methodInvoationsTF.values().toArray(new TFIDF[0]), y.getTFVector().methodInvoationsTF.values().toArray(new TFIDF[0]));
			variableDecVal = CalculateSimilarity(x.getTFVector().variableDeclarationsTF.values().toArray(new TFIDF[0]), y.getTFVector().variableDeclarationsTF.values().toArray(new TFIDF[0]));
		}
		catch (Exception e) {
			System.err.println("X : " + indexOfX + "\nY: " + indexOfY);
		}
		importSimilarity[indexOfX][indexOfY] = importVal;
		importSimilarity[indexOfY][indexOfX] = importVal;
		classInstanceSimilarity[indexOfX][indexOfY] = classInstanceVal;
		classInstanceSimilarity[indexOfY][indexOfX] = classInstanceVal;
		methodInvocationSimilarity[indexOfX][indexOfY] = methodVal;
		methodInvocationSimilarity[indexOfY][indexOfX] = methodVal;
		variableDeclarationSimilarity[indexOfX][indexOfY] = variableDecVal;
		variableDeclarationSimilarity[indexOfY][indexOfX] = variableDecVal;
		
		float similarityVal = importVal * 1 + classInstanceVal * 3 + methodVal * 5 + variableDecVal * 1;
		similarity[indexOfX][indexOfY] = similarityVal;
		similarity[indexOfY][indexOfX] = similarityVal;
		
		sortedSimilarity.add(new Similarity((similarityVal), indexOfX, indexOfY));
		
	}

	public LinkedHashSet<CompilationUnitFacade> GetTopSimilar(int numOfSimilarUnits) {
		LinkedHashSet<CompilationUnitFacade> topSimilar = new LinkedHashSet<CompilationUnitFacade>();
		Collections.sort(sortedSimilarity, new SimilarityComparator());
		Collections.reverse(sortedSimilarity);
		Iterator<Similarity> it = sortedSimilarity.iterator();

		while (topSimilar.size() <= numOfSimilarUnits) {
			try {
			Similarity currSimilarity = it.next();
			if (currSimilarity.similarity == (float) 0) {
				break;
			}
			if ( currSimilarity.x != currSimilarity.y) {
				topSimilar.add(facadeList.get(currSimilarity.x));
				topSimilar.add(facadeList.get(currSimilarity.y));
			}
			}
			catch (NoSuchElementException e ) {
				System.err.println("Less than " + numOfSimilarUnits + " found..");
				break;
			}
		}
		return topSimilar;
	}
	
	private float CalculateSimilarity(TFIDF[] a, TFIDF[] b) throws Exception{
		float dotProduct = 0;
		float magProduct;
		float magA = 0;
		float magB = 0;
		for (int i = 0; i < a.length; i ++) {
			try {
				dotProduct += a[i].getValue()*b[i].getValue();
			}
			catch (Exception e) {
				System.err.println("A : " + a[i].getValue());
				System.err.println("B : " + b[i].getValue());
				e.printStackTrace();
				dotProduct += a[i].getValue()*b[i].getValue();
			}
			magA += a[i].getValue() * a[i].getValue();
			magB += b[i].getValue() * b[i].getValue();
		}
		
		magProduct = (float) ((Math.sqrt(magA)) * (Math.sqrt(magB)));
		return dotProduct / (magProduct + Float.MIN_VALUE);
	}
	
	
	static class SimilarityComparator implements Comparator<Similarity> {

		@Override
		public int compare(Similarity o1, Similarity o2) {
			return (o1.similarity > o2.similarity) ? 1 : 0;
		}

	}
	
	public class Similarity {
		public float similarity;
		public int x;
		public int y;
		
		public boolean equals(Similarity B) {
			boolean flag = true;
			if (this.similarity != B.similarity || this.x != B.x || this.y != B.y) {
				flag = false;
			}
			return flag;
		}
		public Similarity(float s, int X, int Y) {
			this.similarity = s;
			this.x = X;
			this.y = Y;
		}
		public String toString() {
			return similarity + ":" + x + "/" + y;
		}
	}

}
