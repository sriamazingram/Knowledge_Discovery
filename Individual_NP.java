import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

public class Individual_NP {
	static HashMap<String, Integer> np= new HashMap<String,Integer>();
	static HashMap<String, ArrayList<String>> graph_rel= new HashMap<String,ArrayList<String>>();
	static int np_count=0;
	static ArrayList<String> triples= new ArrayList<String>();
	@SuppressWarnings("serial")
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		// TODO Auto-generated method stub
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
	Properties props = PropertiesUtils.asProperties("annotators", "tokenize,ssplit,pos,lemma,depparse,parse");
	StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	File folder=new File("/home/sriamazingram/Scientific_Articles/Dataset/ccgw");
	File[] files = folder.listFiles();
	Arrays.sort(files);
	int cur;
	for(File file:files){

		System.out.println(file.getName());
		cur=Integer.valueOf(file.getName().replace(".xml",""));
    	String [] contents=new String[60],ids=new String[60];int ct=0;
    	Document doc = builder.parse(file.getAbsolutePath());
		doc.getDocumentElement().normalize();
		NodeList list = doc.getElementsByTagName("entry");
		for(int j=0;j<list.getLength();j++)
		{
			System.out.println((j+1)+"\n");
			Node n = list.item(j);
			if(n.getNodeType()==Node.ELEMENT_NODE)
			{
				Element e = (Element) n;
				String title=e.getElementsByTagName("title").item(0).getTextContent(),content=e.getElementsByTagName("content").item(0).getTextContent(),id=e.getElementsByTagName("id").item(0).getTextContent();
				//System.out.println("Title");
				title = title.replaceFirst("<p>.*</p>","");
				title = title.replace("<p>","");
				title = title.replace("</p>","");
				title = title.replace("<i>","");
				title = title.replace("</i>","");
				title = title.replace("<sup>","");
				title = title.replace("</sup>","");
				title = title.replace("<sub>","");
				title = title.replace("</sub>","");
				title.replaceAll("\\s{2,}", " ").trim();
				title = title.trim();
				//parserFunction(pipeline,title);
				content = content.replaceFirst("<p>.*</p>","");
				content = content.replace("<p>","");
				content = content.replace("</p>","");
				content = content.replace("<i>", "");
				content = content.replace("</i>","");
				content = content.replace("<sup>","");
				content = content.replace("</sup>","");
				content = content.replace("<sub>","");
				content = content.replace("</sub>","");
				content.replaceAll("\\s{2,}", " ").trim();
				content = content.trim();
				content =content.replaceFirst("Background", "");
				content =content.replaceFirst("Methods", "");
				contents[ct]=content;
				ids[ct]=id;
				ct++;
			}
		}
		int z=0;
		//System.out.println(contents.length);
		for(String content_line:contents)
		{
			Annotation doc1 = new Annotation(content_line);
	    	try
	    	{
	    		pipeline.annotate(doc1);
	    	}
	    	catch(NullPointerException | OutOfMemoryError e){
	    		continue;
	    	}
	    	for (CoreMap sentence : doc1.get(CoreAnnotations.SentencesAnnotation.class)) {
	              generatesentences(sentence);
	    		for (String triple : triples) {
	    			String [] phrases = triple.split("##");
		    	  String sub=phrases[0];
		    	  String rel=phrases[1];
		    	  String obj=phrases[2];
		    	  if(sub=="" || rel=="" || obj=="" || obj.equals(sub))
		    	  {
		    		  continue;
		    	  }
		    	  String temp_string=rel;
		    	  if(np.containsKey(sub))
		    	  {
		    		  int np_index=np.get(sub);
		    		  if(np.containsKey(obj))
		    		  {
		    			  int obj_index=np.get(obj);
	    			  	  ArrayList<String> x;
	    				  if(graph_rel.containsKey(String.valueOf(np_index)+","+String.valueOf(obj_index)))
	    				  {
	    					  x=graph_rel.get(String.valueOf(np_index)+","+String.valueOf(obj_index));
	    					  x.add(rel);
			    			  graph_rel.put(String.valueOf(np_index)+","+String.valueOf(obj_index),x);
	    				  }
	    				  else
	    				  {
	    					  graph_rel.put(String.valueOf(np_index)+","+String.valueOf(obj_index), new ArrayList<String>() {{add(temp_string);}});
	    				  }
		    		  }
		    		  else 
		    		  {

		    			    np_count+=1;
		    			  	np.put(obj, np_count);
	    				    graph_rel.put(String.valueOf(np_index)+","+String.valueOf(np_count), new ArrayList<String>() {{add(temp_string);}});
		    		  }
		    	  }
		    	  else
		    	  {
		    		  np_count+=1;
	    			  np.put(sub, np_count);
	    			  int np_index=np_count;
		    		  if (np.containsKey(obj))
		    		  {
		    			  int obj_index=np.get(obj);
	    				  graph_rel.put(String.valueOf(np_index)+","+String.valueOf(obj_index), new ArrayList<String>() {{add(temp_string);}});
		    		  }
		    		  else
		    		  {
		    			np_count+=1;
		    			np.put(obj, np_count);
		    			graph_rel.put(String.valueOf(np_index)+","+String.valueOf(np_count), new ArrayList<String>() {{add(temp_string);}});
		    		  }
		    	  }
	        }
	    	}
	    	try
	    	{
	    		printfiles(cur,ids[z]);
	    	}
	    	catch(FileNotFoundException e) {
	    		continue;
	    	}
	    	z++;
	    	np_count=0;
    		graph_rel=null;
    		np=null;
    		System.gc();
    		graph_rel= new HashMap<String,ArrayList<String>>();
    		np= new HashMap<String,Integer>();
    	}
  }
}
	public static void generatesentences(CoreMap sentence)
	{
		triples= new ArrayList <String>();
		HashMap <Tree,Boolean> map=new HashMap <Tree,Boolean>();
		Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
	      for (Tree t:tree)
	      {

	    	  String nptemp="";
	    	  if(map.containsKey(t))
	    		  continue;
	    	if(t.value().equals("NP"))
	    	{
	    		map.put(t,true);
	    		for(Tree x:t) {
	    			map.put(x,true);

	    			if(x.isPrePreTerminal() && x.value().equals("NP")) {
	    				List<?> leaves=x.getLeaves();
	    				nptemp="";
	    				for(int i = 0;i<leaves.size();i++) {
	    					nptemp+=leaves.get(i).toString()+" ";
	    				}
	    				nptemp=nptemp.trim();
	    				break;
	    			}
	    			
	    		}
	    		ArrayList<String> verbs=new ArrayList<String>();
	    		int flag=0;
	    		for(Tree t2:tree)
	    		{
	    			if(flag==0)
	    				if(!t2.equals(t)) {
	    					continue;
	    			}
	    			flag=1;
	    			if(map.containsKey(t2))
	    				continue;
	    			if(t2.value().matches("VB.*"))
	            	{
	            		List<?> leaves=t2.getLeaves();
	            		String vbtemp="";
	            		for(int i=0;i<leaves.size();i++)
	            		{
	            			vbtemp+=leaves.get(i).toString();
	            		}
	            		verbs.add(vbtemp);
	            		
	            	}
	    			if(t2.value().equals("NP")&&t2.isPrePreTerminal()) {
	            				List<?> leaves=t2.getLeaves();
	            				String nptemp2="";
	            				for(int i = 0;i<leaves.size();i++) {
	            					nptemp2+=leaves.get(i).toString()+" ";
	            				}
	            				nptemp2=nptemp2.trim();
	            				for(String vt:verbs)
	            				{
	            					triples.add(nptemp+"##"+vt+"##"+nptemp2);
	            					System.out.println(nptemp+"##"+vt+"##"+nptemp2);
	            				}
	            			}
	            	
	    		}
	    		
	        	
	    		
	    	}
	    	  
	      }

	}

  public static void printfiles(int i,String id) throws IOException
  {
	  id=id.replaceAll("/", "_");
	  System.out.println(id);
      PrintWriter writer1 = new PrintWriter("/home/sriamazingram/Scientific_Articles/FGraphs/np_"+String.valueOf(i)+"_"+id);
      PrintWriter writer2 = new PrintWriter("/home/sriamazingram/Scientific_Articles/FGraphs/rel_"+String.valueOf(i)+"_"+id);
      PrintWriter writer3 = new PrintWriter("/home/sriamazingram/Scientific_Articles/FGraphs/sentences_"+String.valueOf(i)+"_"+id+".txt");
	  int filenum=i;
      for (Map.Entry<String, ArrayList<String>> entry:graph_rel.entrySet())
      {
    	  writer2.println(entry.getKey());
    	  for(String v:entry.getValue())
    	  {
    		  writer2.println(v);
    	  }
	  }
     Map<String, Integer> result = sortByValue(np);
     for (Map.Entry<String, Integer> entry : result.entrySet()) {
         writer1.println(entry.getKey()+" "+String.valueOf(entry.getValue()));
     }
      writer1.close();
      writer2.close();
      BufferedReader reader1=new BufferedReader(new FileReader("/home/sriamazingram/Scientific_Articles/FGraphs/score_"+String.valueOf(filenum)+".txt")),reader2=new BufferedReader(new FileReader("/home/sriamazingram/Scientific_Articles/FGraphs/weights_"+String.valueOf(filenum)+".txt")),reader3=new BufferedReader(new FileReader("/home/sriamazingram/Scientific_Articles/FGraphs/np_"+String.valueOf(filenum)));
      String l1=reader1.readLine();
      HashMap<String,Double> scores=new HashMap<String,Double>();
      while(l1!=null)
      {
    	  String [] s=l1.trim().split(" ");
    	  if(np.containsKey(String.join(" ", Arrays.copyOfRange(s, 0, s.length-1)).trim()))
    	  {
    		  scores.put(String.join(" ", Arrays.copyOfRange(s, 0, s.length-1)).trim(),Double.valueOf(s[s.length-1]));
    	  }
    	  l1=reader1.readLine();
      }
      reader1.close();
      Map<String,Double> sorted_scores=sortByValue_rev(scores);
      HashMap<String,Integer> full_np=new HashMap<String,Integer>();
      l1=reader3.readLine();
      while(l1!=null)
      {
    	  String [] s=l1.trim().split(" ");
    	  if(np.containsKey(String.join(" ", Arrays.copyOfRange(s, 0, s.length-1)).trim()))
    	  {
    		  full_np.put(String.join(" ", Arrays.copyOfRange(s, 0, s.length-1)).trim(),Integer.valueOf(s[s.length-1]));
    	  }
    	  l1=reader3.readLine();
      }
      reader3.close();
      l1=reader2.readLine();
      int n=1;
      HashMap<Integer,Double []> weights=new HashMap<Integer,Double[]>();
      while(l1!=null)
      {
    	  l1=reader2.readLine();
    	  if(l1==null)
    		  break;
    	  String [] s=l1.trim().split(" ");
    	  Double [] l=new Double[s.length];
    	  for(int q=0;q<s.length;q++)
    	  {
    		  l[q]=Double.valueOf(s[q]);
    	  }
    	  weights.put(n,l);
    	  n++;
      }
      reader2.close();
      for(String k:sorted_scores.keySet())
      {
    	  int np1_index=full_np.get(k);
    	  Double [] neighbors1=weights.get(np1_index);
    	  if(neighbors1!=null)
    	  {
    		  neighbors1=sortRelations(neighbors1);
    		  for(int j=0;j<neighbors1.length;j+=2)
	    	  {
    			  int flag=0;
    			  String x="";
	    		  for(String m:full_np.keySet())
	    		  {
	    			  if(full_np.get(m)==neighbors1[j].intValue())
	    			  {
	    				  x=m;
	    				  break;
	    			  }
	    		  }
	    		  if(x!="")
	    		  {
	    			  if(graph_rel.containsKey(String.valueOf(np.get(k))+","+String.valueOf(np.get(x))))
	    			  {
	    				  ArrayList<String> rels=graph_rel.get(String.valueOf(np.get(k))+","+String.valueOf(np.get(x)));
	    				  String v1=maxElement(rels);
	    				  Double [] neighbors2=weights.get(full_np.get(x));
		    			  if(neighbors2!=null)
		    			  {
		    				  neighbors2=sortRelations(neighbors2);
		    				  for(int p=0;p<neighbors2.length;p+=2)
		    				  {
		    					  String y="";
		    					  for(String m:full_np.keySet())
		    					  {
		    						  if(full_np.get(m)==neighbors2[p].intValue())
		    						  {
		    							  y=m;
		    							  break;
		    						  }
		    					  }
		    					  if(y!="")
		    					  {
		    						  if(graph_rel.containsKey(String.valueOf(np.get(x))+","+String.valueOf(np.get(y))))
		    						  {
		    							  ArrayList<String> rels2=graph_rel.get(String.valueOf(np.get(x))+","+String.valueOf(np.get(y)));
		    							  String v2=maxElement(rels2);
		    							  System.out.println(scores.get(k)+" "+k+" "+v1+" "+x+" "+v2+" "+y);
		    							  writer3.println(scores.get(k)+" "+k+" "+v1+" "+x+" "+v2+" "+y);
		    							  flag=1;
		    							  break;
		    						  }
		    					  }
		    				  }
		    			  }
	    			  }
	    		  }
	    		  if(flag==1)
	    			  break;
	    	  }
    	  }
      }
      writer3.close();
  }
  private static Double[] sortRelations(Double[] neighbors) {
	for(int i=0;i<neighbors.length;i+=2)
	{
		Double min=neighbors[i+1];
		int min_index=i;
		if(i+2<neighbors.length)
		{
			Double min1=neighbors[i+3];int min1_index=i+2;
			for(int j=i+2;j<neighbors.length;j+=2)
			{
				if(neighbors[j+1]<min1)
				{
					min1=neighbors[j+1];
					min1_index=j;
				}
			}
			if(min1<min)
			{
				Double temp=neighbors[min_index];
				neighbors[min_index]=neighbors[min1_index];
				neighbors[min1_index]=temp;
				temp=neighbors[min_index+1];
				neighbors[min_index+1]=neighbors[min1_index+1];
				neighbors[min1_index+1]=temp;
			}
		}
	}
	return neighbors;
}
static String maxElement(ArrayList<String> rels)
  {
	  HashMap<String,Integer> count=new HashMap<String,Integer>();
	  for(String i:rels)
	  {
		  if(count.containsKey(i))
		  {
			  int c=count.get(i);c++;
			  count.put(i,c);
		  }
		  else
		  {
			  count.put(i,1);
		  }
	  }
	  int c=-1;
	  String r = "";
	  for(String k:count.keySet())
	  {
		  if(count.get(k)>c)
		  {
			  r=k;
			  c=count.get(k);
		  }
	  }
	  return r;
  }
  public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue_rev(Map<K, V> map) {
	    return map.entrySet()
	              .stream()
	              .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
	              .collect(Collectors.toMap(
	                Map.Entry::getKey, 
	                Map.Entry::getValue, 
	                (e1, e2) -> e1, 
	                LinkedHashMap::new
	              ));
	}
  public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
	    return map.entrySet()
	              .stream()
	              .sorted(Map.Entry.comparingByValue(/*Collections.reverseOrder()*/))
	              .collect(Collectors.toMap(
	                Map.Entry::getKey,
	                Map.Entry::getValue,
	                (e1, e2) -> e1,
	                LinkedHashMap::new
	              ));
	}
}
