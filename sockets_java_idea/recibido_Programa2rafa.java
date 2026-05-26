/*
Numero programa: 02
Nombre: Carlos Rafael Ortiz Bañuelos
Fecha: 17/04/26
Descripcion: Programa que analiza un archivo y cuenta sus
lineas logicas o LOC de acuerdo a los estandares de conteo 
y codificación
*/

//paquetes
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

//clase padre que contiene el programa
public class Programa2{

public Programa2(){
	
}//constructor

//clase encargada de la lectura de archivos con la extensión correcta
static class LectorArchivo {
	
	public void LectorArchivo(){
		String entrada = lectura.nextLine().trim();
	}
	
    private Scanner lectura = new Scanner(System.in);
    private String nombreArchivo;
	private String numeroPrograma;

    void introducirNombre() {
        while (true) {
            System.out.print("Introduce el nombre del  archivo (debe terminar en .java): ");
            String entrada = lectura.nextLine().trim();

            if (entrada.isEmpty()) {
                System.out.println("Nombre invalido, no  puede ser vacio");
                continue;
            }//cierra if

            if (!entrada.toLowerCase().endsWith(".java")) {
                System.out.println("Nombre invalido, tiene que tener la extension .java\n" +
                "ej: archivo.java");
                continue;
            }//cierra if

            if (entrada.length() == 5) {
                System.out.println("Nombre invalido, tiene  que tener un nombre antes del: .java");
                continue;
            }//cierra if

            nombreArchivo = entrada.substring(0,entrada.length() - 5);
			numeroPrograma = nombreArchivo.replaceAll(".*?(\\d+)$", "$1");
            break;
        }/*cierra while*/
    }/*cierra inttroducirNombre*/
	
		//lee las lineas del archivo
		List<String> leerLineas(){ 
		List<String> lineas = new ArrayList<String>();
		String rutaArchivo = nombreArchivo + ".java";
		
		try{
			BufferedReader lectorArchivo = new BufferedReader(
				new FileReader(rutaArchivo));
				String lineaActual;
				String lineaAcumulada = "";
				
				while((lineaActual = lectorArchivo.readLine()) != null){
					String lineaTrim = lineaActual.trim();
					
					if(lineaTrim.endsWith("\\")){
						lineaAcumulada += lineaTrim.substring(0,
						lineaTrim.length() -1);
						
						
					}/*cierra if*/else{
						lineaAcumulada += lineaTrim;
						
						if(!lineaAcumulada.isEmpty()){
							lineas.add(lineaAcumulada);
						}//cierra if
						lineaAcumulada = "";
					}//cierra else
				}//cierra while
			
				lectorArchivo.close();
		}//cierra try
		catch (IOException e){
			System.out.println("Error: no se encontro el archivo: " + rutaArchivo);
		}//cierra catch
		
		return lineas;
		}//cierra leerLineas
		
		String getNombreArchivo(){
			return nombreArchivo;
		}//cierra getNombreArchivo
		
		String getNumeroPrograma(){
			return numeroPrograma;
		}//cierra getNumeroPrograma
    }/*cierra lectorArchivo*/

//clase encargada del conteo de LOCS
 static   class Analisis{
        //variables
        private List<String> nombresClases = new ArrayList<String>();
        private List<Integer> metodosPorClase = new ArrayList<Integer>();
        private List<Integer> locPorClase = new ArrayList<Integer>();
        private List<String> varsInicializadas = new ArrayList<String>();
        private int locTotal = 0;

        //Metodo para analizar lineas de un archivo
    void analizarArchivo(List<String>lineas){
        int indiceClaseActual = -1;
        boolean dentroDeClase = false;
        boolean dentroDeMetodo = false;
        int llaveClase = 0;
        int llaveMetodo = 0;

    for(String lineaRaw : lineas){
        String linea = lineaRaw.trim();

        //saltar lineas en blanco
        if(esBlanco(linea) || esComentario(linea)) continue;
		
		

        //ver si se declaro una clase
        if(esDeclaracionClase(linea)){
			String nombre = extraerNombreClase(linea);
			if(nombre != null){
            nombresClases.add(extraerNombreClase(linea));
            metodosPorClase.add(0);
            locPorClase.add(0);
            indiceClaseActual = nombresClases.size() - 1;
            dentroDeClase = true;
            llaveClase = 0;
			}//cierra if
         }//cierra if
		 
         //ver donde cierran las llaves de las clases
         if(dentroDeClase){
             for(char c : linea.toCharArray()){
				if(c == '{') llaveClase++;
				if(c == '}') llaveClase--;
			}//cierra for
             if(llaveClase <= 0){
                 dentroDeClase = false;
                 dentroDeMetodo = false;
			}//cierra if
		}//cierra if

    //verificar si se declaro un metodo
    if(dentroDeClase && esDeclaracionMetodo(linea)){
		dentroDeMetodo = true;
		llaveMetodo = 0;
		int actual = metodosPorClase.get(indiceClaseActual);
		metodosPorClase.set(indiceClaseActual, actual + 1);
	}//cierra if

    //ver donde cierran las llaves de los metodos
    if(dentroDeMetodo){
        for(char c : linea.toCharArray()){
            if(c == '{') llaveMetodo++;
            if(c == '}') llaveMetodo--;
		}//cierra for
    if(llaveMetodo <=0)dentroDeMetodo = false;
	}//cierra if

    //contar lineas
    if(esLocLogica(linea)){
        locTotal++;
        if(indiceClaseActual >=0){
            int locActual = locPorClase.get(indiceClaseActual);
            locPorClase.set(indiceClaseActual, locActual + 1);
		}//cierra if
	}//cierra if

    //ver si una variable se declaro e inicializo en la misma linea
    if(esVarInicializada(linea)){
        varsInicializadas.add(linea);
	}//cierra if
	}//cierra for
}//cierra analizarArchivo

	//Metodos auxiliares
	
	private String quitarComentarioInline(String linea){
		boolean dentroDeString = false;
		for(int i = 0; i < linea.length() -1; i++){
			char c = linea.charAt(i);
			if(c == '"')dentroDeString = !dentroDeString;
			if(!dentroDeString && c == '/' &&
				linea.charAt(i + 1) == '/'){
					return linea.substring(0, i).trim();
				}//cierra if
		}//cierra for
		return linea.trim();
	}//cierra quitarComentarioInline
	
	//metodo para no contar lineas vacias
	private boolean esBlanco(String linea){
		return linea.isEmpty();
	}//cierra esBlanco
	
	private boolean esComentario(String linea){
		return linea.startsWith("//")||
			   linea.startsWith("/*")||
			   linea.startsWith("*");
	}//cierra esComentario
	
	//cuenta los nombres de las clases como LOC
	private boolean esDeclaracionClase(String linea){
		return linea.matches(
		"^(public|private|protected|static|abstract|final|\\s)*" +
		"class\\s+\\w+.*\\{?$");
		
	}//cierra esDeclaracionClase
	
	//Extrae solo el nombre de la clase
	private String extraerNombreClase(String linea){
		String[] partes = linea.split("\\s+");
		for (int i = 0; i < partes.length - 1; i++){
			if(partes[i].equals("class")){
				String nombre = partes[i + 1].replace("{", "").trim();
				
				if(!nombre.isEmpty() && nombre.matches("[A-Za-z]\\w*")){
					return nombre;
				}//cierra if
			}//cierra if
		}//cierra for
		return null;
	}//cierra extraerNombreClase
	
	//Detecta metodos y los cuenta como loc
	private boolean esDeclaracionMetodo(String linea){
		boolean esMetodo = linea.matches(
			".*(public|private|protected|void|static).*\\(.*\\).*\\{?")
			&& !esDeclaracionClase(linea);
			
			//detectar constructores como metodos
		boolean esConstructor = linea.matches(
			"(public|private|protected)?\\s*[A-Z]\\w*\\s*\\(.*\\)\\s*\\{?")
			&& !esDeclaracionClase(linea);
			return esMetodo || esConstructor;
	}//cierra esDeclaracionMetodo
	
	//lineas que no cuentan como loc
	private boolean esLocLogica(String linea){
		
		String lineaEval = quitarComentarioInline(linea);
		
		//no contar las llaves de cierre
		if(lineaEval.equals("}") ||
			lineaEval.equals("};") ||
			lineaEval.startsWith("}//")||
			lineaEval.startsWith("}/*")) return false;
			
		if(lineaEval.endsWith("}")){
			lineaEval = lineaEval.substring(0,
						lineaEval.length() - 1).trim();
		}//cierra if
		
		//linea vaciar comentarios
	if(esBlanco(lineaEval))return false;
		
		//solo comentarios
	if(esComentario(lineaEval))return false;
	
	
	return linea.endsWith(";")||
		   linea.endsWith("{")||
		   linea.matches("case .+:")||
		   linea.equals("default:")||
		   linea.matches(".*\\)\\s*\\{?");
	}//cierra esLocLogica
	
	//ver si cualquier tipo de  variable se inicializo y asigno en misma linea
	private boolean esVarInicializada(String linea){
	
    //quitar comentarios al final de variables declaradas e inicializadas en una linea
    String lineaLimpia = quitarComentarioInline(linea);
	
	  // Limpiar palabras reservadas de los tipos de datos
	lineaLimpia = lineaLimpia
		.replace("private", "")
		.replace("public", "")
		.replace("protected", "")
		.replace("static", "")
		.replace("final", "")
		.replaceAll("\\s+", " ")
		.trim();


    if(!lineaLimpia.contains("=")) return false;
    if(!lineaLimpia.endsWith(";")) return false;

    String[] partesIgual = lineaLimpia.split("=", 2);
    if(partesIgual.length < 2) return false;

    String tipoYNombre = partesIgual[0].trim();
    String[] palabras  = tipoYNombre.split("\\s+");

    if(palabras.length != 2) return false;
    if(!palabras[0].matches("[A-Za-z][\\w<>\\[\\],]*")) return false;
    if(!palabras[1].matches("[a-zA-Z_]\\w*")) return false;

    return true;
}//cierra esVarInicializada
	
	
	//getters
	List<String> getNombresClases() { return nombresClases;
	}
	List<Integer> getMetodosPorClase() { return metodosPorClase;
	}
	List<Integer> getLocPorClase() {return locPorClase; 
	}
	List<String> getVarsInicializadas() { return varsInicializadas;
	}
	int getLocTotal() {return locTotal;
	}
	
}//cierra Analisis
    

//clase que presenta los resultados en forma de tabla
 static   class Presentacion{
		//metodo para mostrar los resultados
	    void mostrarResultados(Analisis analisis, String nombreArchivo, String numeroPrograma){
			List<String> nombres = analisis.getNombresClases();
			List<Integer> metodos = analisis.getMetodosPorClase();
			List<Integer> locClase = analisis.getLocPorClase();
			
			System.out.println("Archivo leido: " + nombreArchivo + ".java\n");
			
			//Tabla
			mostrarTablaClases(nombres, metodos, locClase, analisis.getLocTotal(),numeroPrograma);
			
			mostrarVarsInicializadas(analisis.getVarsInicializadas());
		}//cierra mostrarResultados
		
		private void mostrarTablaClases(List<String> nombres,
		List<Integer> metodos,List<Integer> locClase, int locTotal, String numeroPrograma){
			System.out.printf("%-10s %-20s %-20s %-20s %-15s%n",
			"Numero Prog", "Nombre de clase", "Numero de metodos", "Tamanio de clase","Tamanio total");
			for(int i = 0; i < 87; i++)System.out.print("-");
			System.out.println();
			
			for(int i = 0; i < nombres.size(); i++){
			
			if(i == nombres.size() -1){
				System.out.printf("%-10s %-20s %-20d %-20d %-15d%n",
					numeroPrograma,
					nombres.get(i),
					metodos.get(i),
					locClase.get(i),
					locTotal);
			}/*cierra if*/ else{
				System.out.printf("%-10s %-20s %-20s %-20d %-15s%n",
					numeroPrograma,
					nombres.get(i),
					metodos.get(i),
					locClase.get(i),
					"");
			}//cierra else
			}//cierra for
		
			for(int i = 0; i < 87; i++)System.out.print("-");
			System.out.println();
		}//cierra mostrarTablaClases
		
		//variables declaradas e inicializadas en la misma linea
		private void mostrarVarsInicializadas(List<String> vars){
			System.out.println();
			
			if(vars.isEmpty()){
				System.out.println(
					" No se encontraron variables declaradas" +
					" e inicializadas en la misma linea.");
				return;
			}//cierra if
			
			for(String var : vars){
				System.out.println(" Variable declarada e  inicializada" +
				" en la misma linea: " + var);
			}//cierra for
		}//cierra mostrarVarsInicializadas
    }//cierra Presentacion

public static void main(String[]args){
	LectorArchivo lector = new LectorArchivo();
	Analisis analisis = new Analisis();
	Presentacion presentacion = new Presentacion();
	
	//se pide el nombre
	lector.introducirNombre();
	//se leen las lineas
	List<String> lineas = lector.leerLineas();
	//se analiza el archivo
	analisis.analizarArchivo(lineas);
	//se musstran los resultados
	presentacion.mostrarResultados(analisis, lector.getNombreArchivo(),lector.getNumeroPrograma());
}//cierra metodo main
}//cierra prog2