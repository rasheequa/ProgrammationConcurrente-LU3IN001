package pc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import pc.test.TaillesNonConcordantesException;

public class MatriceEntiere {
	private int [][] matrice;
	
	public MatriceEntiere(int ligne, int colonne) {
		
		matrice = new int [ligne][];
		
		for(int i = 0; i<matrice.length;i++) {
			matrice[i]= new int [colonne];
			for(int j = 0; j<matrice[i].length;j++) {
					matrice[i][j]= 0;
			}				
		}
		
	}
	
	public int getElem(int lig, int col) {
		return matrice[lig][col];
	}
	
	public void setElem(int lig, int col, int val) {
		matrice[lig][col]=val;
		
	}
	
	 public int nbLignes() {
		 return matrice.length;
	 }
	 
	 public int nbColonnes() {
		 return matrice[0].length;
	 }
	 
	 public static MatriceEntiere parseMatrix(File fichier) throws
	 IOException{
		 try (BufferedReader in = new BufferedReader ( new FileReader(fichier))){
			 String nbligStr = in.readLine();
			 int nblig = Integer.parseInt(nbligStr);
			 int nbcol = Integer.parseInt (in.readLine());
			 MatriceEntiere mat = new MatriceEntiere(nblig,nbcol);
			 for(int i = 0; i < nblig; i++) {
				 String line = in.readLine();
				 String[] word = line.split(" ");
				 for(int j = 0; j<nbcol; j++) {
					 mat.matrice[i][j] = Integer.parseInt(word[j]);
				 }
			 }
			return mat;
		 }
		 catch (Exception e) { throw new IOException(e);
		 }
		 
	 }
	 
	 public String toString() {
		 String s = "";
		 for(int[] mat : matrice) {
			 for(int val : mat) {
				 s+= val+" ";
			 } 
			 s += "\n";
		 }
		  
		 return s;
	 }
	 
	 public boolean equals(Object o) {
		 if (this == o) return true;
		 
		 
		 if (o instanceof MatriceEntiere) {
			 
			 if (this.nbLignes()!= ((MatriceEntiere) o).nbLignes()) return false;
			 
			 for(int i = 0; i<this.matrice.length;i++) {
				 for(int j =0; j<this.matrice[i].length; j++) {
					 if (((MatriceEntiere)o).matrice[i][j]!=this.matrice[i][j]) {
						 return false;
					 }
				 }
			 } 
			 return true;
		 }
		 return false;
	 }
	 
	 public MatriceEntiere ajoute(MatriceEntiere m) throws TaillesNonConcordantesException{
		 if(this.nbLignes()!= ( m.nbLignes())){
	            throw new TaillesNonConcordantesException("Les dimensions ne sont pas concordantes : "+this.nbLignes()+" != "+m.nbLignes());
	        }
		 
		 MatriceEntiere mat = new MatriceEntiere(this.nbLignes(),this.nbLignes());
		 
		 for(int i = 0; i<m.matrice.length;i++) {
				for(int j = 0; j<m.matrice[i].length;j++) {
						mat.matrice[i][j]=m.matrice[i][j]+this.matrice[i][j] ;
				}			
		 }
		 return mat;
	 }
	 
	 public MatriceEntiere produit(MatriceEntiere m) throws TaillesNonConcordantesException{
		 
		 if(this.nbColonnes()!= ( m.nbLignes())){
	            throw new TaillesNonConcordantesException("Les dimensions ne sont pas concordantes : "+this.nbLignes()+" != "+m.nbColonnes());
	        }
		 
		 MatriceEntiere mat = new MatriceEntiere(this.nbLignes(),this.nbLignes());
		 int sum = 0, j=0;
		 for(int k=0; k<m.matrice.length;k++) {
			 for(int i = 0; i<m.matrice.length;i++) {
				 for(j = 0; j<m.matrice.length;j++) {
					sum += this.matrice[i][k] * m.matrice[k][j] ;
				 }
			 mat.matrice[i][j]=sum;
			 }
		 }
		 return mat;
	 }
	 
	 public MatriceEntiere transposee() {
		 
		 MatriceEntiere mat = new MatriceEntiere(this.nbLignes(),this.nbLignes());
		
		 for(int i = 0; i<this.matrice.length;i++){
			 for(int j = 0; j<this.matrice.length;j++){
				mat.matrice[i][j] = this.matrice[j][i];
			 } 
		 }
		 return mat;
	 }
	
}
