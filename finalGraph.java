
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.naturalli.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("unused")
public class finalGraph {
	static HashMap<String, Integer> np= new HashMap<String,Integer>();
	static HashMap<Integer, Set<Integer>> graph= new HashMap<Integer,Set<Integer>>();
	static HashMap<String, ArrayList<String>> graph_rel= new HashMap<String,ArrayList<String>>();
	static int np_count=0;
	static ArrayList<String> triples= new ArrayList<String>();
	@SuppressWarnings("serial")
    public static void main(String[] args) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
	Properties props = PropertiesUtils.asProperties("annotators", "tokenize,ssplit,pos,lemma,depparse,parse");
	StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	File folder=new File("/home/sriamazingram/Scientific_Articles/Dataset/ccgw");
	File[] files = folder.listFiles();
	Arrays.sort(files);
	int init = 0;
	int prev=0,cur;
	for(File file:files){

		System.out.println(file.getName());
		if(init==0)
		{

			prev=Integer.valueOf(file.getName().replace(".xml",""));
		}
		init=1;
		cur=Integer.valueOf(file.getName().replace(".xml",""));
    	if(init==1 && cur!=prev)
    	{

    		printgraph(prev);
    		np_count=0;
    		prev=Integer.valueOf(file.getName().replace(".xml",""));
    		graph = null;
    		graph_rel=null;
    		np=null;
    		System.gc();
    		graph= new HashMap<Integer,Set<Integer>>();
    		graph_rel= new HashMap<String,ArrayList<String>>();
    		np= new HashMap<String,Integer>();
    		break;
    	}
    	String [] contents=new String[60];int ct=0;
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
				String title=e.getElementsByTagName("title").item(0).getTextContent(),content=e.getElementsByTagName("content").item(0).getTextContent();
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
				ct++;
			}
		}
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
		    		  Set<Integer> temp=graph.get(np_index);
		    		  if(np.containsKey(obj))
		    		  {
		    			  int obj_index=np.get(obj);
		    			  if(temp.contains(obj_index))
		    			  {
		    				  ArrayList<String> x;
		    				  if(graph_rel.containsKey(String.valueOf(np_index)+","+String.valueOf(obj_index)))
		    				  {

		    					  x=graph_rel.get(String.valueOf(np_index)+","+String.valueOf(obj_index));
		    					  x.add(rel);
				    			  graph_rel.put(String.valueOf(np_index)+","+String.valueOf(obj_index),x);

		    				  }
		    				  else
		    				  {

		    					  x=graph_rel.get(String.valueOf(obj_index)+","+String.valueOf(np_index));
		    					  x.add(rel);
				    			  graph_rel.put(String.valueOf(obj_index)+","+String.valueOf(np_index),x);
		    				  }

		    			  }
		    			  else {


		    				    temp.add(obj_index);
		    				    graph.put(np_index,temp);
		    				    temp=graph.get(obj_index);
		    				    temp.add(np_index);
		    				    graph.put(obj_index,temp);
		    				    graph_rel.put(String.valueOf(np_index)+","+String.valueOf(obj_index), new ArrayList<String>() {{add(temp_string);}});

		    			  }

		    		  }
		    		  else {

		    			    np_count+=1;
		    			  	np.put(obj, np_count);
		    			  	temp.add(np_count);
	    				    graph.put(np_index,temp);
	    				    graph.put(np_count,new HashSet<Integer>() {{add(np_index);}});
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

		    			  graph.put(np_count,new HashSet<Integer>() {{add(obj_index);}});
		    			  Set<Integer> temp=graph.get(obj_index);
	    				  temp.add(np_count);
	    				  graph.put(obj_index,temp);
	    				  graph_rel.put(String.valueOf(obj_index)+","+String.valueOf(np_count), new ArrayList<String>() {{add(temp_string);}});

		    		  }
		    		  else
		    		  {

		    			np_count+=1;
		    			np.put(obj, np_count);
		    			graph.put(np_count,new HashSet<Integer>() {{add(np_index);}});
		    			graph.put(np_index,new HashSet<Integer>() {{add(np_count);}});
		    			graph_rel.put(String.valueOf(np_index)+","+String.valueOf(np_count), new ArrayList<String>() {{add(temp_string);}});
		    		  }


		    	  }
	        }

	    	}
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

  public static void printgraph(int i) throws FileNotFoundException
  {
	  System.out.println(graph.keySet().toString());
	  int sub_count=0;
	  int verb_count=0;
	  PrintWriter writer = new PrintWriter("/home/sriamazingram/Scientific_Articles/FGraphs/graph_"+String.valueOf(i));
      PrintWriter writer1 = new PrintWriter("/home/sriamazingram/Scientific_Articles/FGraphs/np_"+String.valueOf(i));
      PrintWriter writer2 = new PrintWriter("/home/sriamazingram/Scientific_Articles/FGraphs/rel_"+String.valueOf(i));
	  int edges=0;
	  int filenum=i;
	  for(i=0;i<graph.size();i++)
	  {
		  edges+=graph.get(i+1).size();
	  }
	  System.out.println(edges);
	  int nodes=graph.size();
      writer.println(nodes+" "+edges/2+" 1");
      for (i=0;i<graph.size();i++)
      {

	        Set<Integer> value = graph.get(i+1);
	        String np1=String.valueOf(i+1);
	        ArrayList<String> temp;
	        for(Integer x:value)
	        {
	        	String np2=String.valueOf(x);
	        	if(graph_rel.containsKey(np2+","+np1))
	        	{
	        		temp=graph_rel.get(np2+","+np1);
	        	}
	        	else
	        	{
	        		temp=graph_rel.get(np1+","+np2);
	        	}
	        	writer.print(np2+" "+String.valueOf(temp.size())+" ");
	        	writer2.println(np1+","+np2);
	        	for(String rels:temp)
	        	{
	        		writer2.println(rels);
	        	}
	        	writer2.println();

	        }
	        writer.println();

	  }


     Map<String, Integer> result = sortByValue(np);
     for (Map.Entry<String, Integer> entry : result.entrySet()) {
         writer1.println(entry.getKey()+" "+String.valueOf(entry.getValue()));
     }

	  writer.close();
      writer1.close();
      writer2.close();

      try {
    	  Process p = Runtime.getRuntime().exec("/home/sriamazingram/Downloads/graclus1.2/graclus graph_"+String.valueOf(filenum)+" "+String.valueOf(nodes/10), null, new File("/home/sriamazingram/Scientific_Articles/FGraphs"));
		//Runtime.getRuntime().exec("cd /home/sriram/New_Articles_Work/FGraphs;graclus graph_"+String.valueOf(filenum)+" "+String.valueOf(nodes));
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
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