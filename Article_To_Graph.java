import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import edu.stanford.nlp.semgraph.SemanticGraph;
//import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

public class Article_To_Graph {
	public static Map<String,ArrayList<String>> npvpnp = new HashMap<String,ArrayList<String>>();
	public static Map<String,Integer> np = new HashMap<String,Integer>();
	public static Map<String,ArrayList<String>> rels = new HashMap<String,ArrayList<String>>();
	public static Map<Integer,Set<Integer>> a_list = new HashMap<Integer,Set<Integer>>();
	public static int np_count=0;
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		File folder=new File("/home/sriamazingram/Scientific_Articles/Dataset/ccgw/");
		File[] files = folder.listFiles();
		for(int p=1;p<=files.length;p++)
		{
			Document doc = builder.parse("/home/sriamazingram/Scientific_Articles/Dataset/ccgw/"+String.valueOf(p)+".xml");
			doc.getDocumentElement().normalize();
			NodeList list = doc.getElementsByTagName("entry");
			Properties props = PropertiesUtils.asProperties("annotators", "tokenize,ssplit,pos,lemma,depparse,parse,natlog,ner,mention,coref,openie");
			//props.setProperty("openie.max_entailments_per_clause","1");
			//props.setProperty("openie.splitter.nomodel","true");
			props.setProperty("openie.resolve_coref", "true");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
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
					//System.out.println("Content");
					parserFunction(pipeline,content);
				}
				for(Entry<String, ArrayList<String>> entry :npvpnp.entrySet())
				{
					String np1=entry.getKey();
					List<String> pq=entry.getValue();
					for(String pair:pq)
					{
						System.out.println("\""+np1+"\"->"+pair+";");
					}
				}
			}
			printGraph(p);
			np_count=0;
			a_list = null;
    		rels=null;
    		np=null;
    		System.gc();
    		a_list= new HashMap<Integer,Set<Integer>>();
    		rels= new HashMap<String,ArrayList<String>>();
    		np= new HashMap<String,Integer>();
		}
	}
	private static void printGraph(int i) throws FileNotFoundException {
		// TODO Auto-generated method stub
		try {
			PrintWriter writer = new PrintWriter("/home/sriamazingram/Scientific_Articles/Results/graph_"+String.valueOf(i));
			PrintWriter writer1 = new PrintWriter("/home/sriamazingram/Scientific_Articles/Results/np_"+String.valueOf(i));
		    PrintWriter writer2 = new PrintWriter("/home/sriamazingram/Scientific_Articles/Results/rel_"+String.valueOf(i));
		    int f=i,edges=0;
		    for(i=0;i<a_list.size();i++) 
			{
				edges+=a_list.get(i+1).size();
			}
		    int nodes=a_list.size();
		    writer.println(nodes+" "+edges/2+" 1");
		    for (i=0;i<a_list.size();i++) 
		    {
		    	    
			        Set<Integer> value = a_list.get(i+1);
			        String np1=String.valueOf(i+1);
			        ArrayList<String> temp;
			        for(Integer x:value)
			        {
			        	String np2=String.valueOf(x);
			        	if(rels.containsKey(np2+","+np1)) 
			        	{
			        		temp=rels.get(np2+","+np1);
			        	}
			        	else
			        	{
			        		temp=rels.get(np1+","+np2);
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
		      @SuppressWarnings("unused")
			Process p = Runtime.getRuntime().exec("/home/sriamazingram/Downloads/graclus1.2/graclus graph_"+String.valueOf(f)+" "+String.valueOf(nodes/10), null, new File("/home/sriamazingram/Scientific_Articles/Results/"));
		      
		} catch (Exception e) {
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
	@SuppressWarnings("serial")
	private static void parserFunction(StanfordCoreNLP pipeline, String content) {
		// TODO Auto-generated method stub
	    Annotation doc = new Annotation(content);
	    pipeline.annotate(doc);
	    int i=1;
	    for(CoreMap sentence:doc.get(CoreAnnotations.SentencesAnnotation.class))
	    {
	    	System.out.println(i+":"+sentence.get(CoreAnnotations.TextAnnotation.class));
	    	//System.out.println(sentence.get());
	    	Collection<RelationTriple> triples=sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);;
	    	for(RelationTriple triple:triples)
	    	{
	    		//System.out.println(triple.subjectGloss()+"\t"+triple.relationGloss()+"\t"+triple.objectGloss());
	    		String sub=triple.subjectLemmaGloss(),rel=triple.relationLemmaGloss(),obj=triple.objectLemmaGloss();
	    		if(triple.isPrefixBe())
	    		{
	    			rel=rel.replaceFirst("be ","");
	    		}
	    		if(sub=="" || rel=="" || obj=="" || obj.equals(sub))
		    	{
	    			//System.out.println("Kill Vaishnav");
	    			continue;
		    	}
	    		if(np.containsKey(sub))
	    		{
	    			int sub_index = np.get(sub);
	    			Set<Integer> temp = a_list.get(sub_index);
	    			if(np.containsKey(obj))
	    			{
	    				int obj_index = np.get(obj);
	    				if(temp.contains(obj_index))
	    				{
							ArrayList<String> x;
	    					if(rels.containsKey(String.valueOf(sub_index)+","+String.valueOf(obj_index)))
	    					{
	    						System.out.println("1");
	    						x=rels.get(String.valueOf(sub_index)+","+String.valueOf(obj_index));
	    						x.add(rel);
	    						rels.put(String.valueOf(sub_index)+","+String.valueOf(obj_index), x);
	    						System.out.println(x);
	    					}
	    					else
	    					{
	    						System.out.println("2");
	    						x=rels.get(String.valueOf(obj_index)+","+String.valueOf(sub_index));
	    						x.add(rel);
	    						rels.put(String.valueOf(obj_index)+","+String.valueOf(sub_index), x);
	    						System.out.println(x);
	    					}
	    				}
	    				else
	    				{
	    					System.out.println("3");
	    					temp.add(obj_index);
	    					a_list.put(sub_index, temp);
	    					Set<Integer> temp2 = a_list.get(obj_index);
	    					temp2.add(sub_index);
	    					a_list.put(obj_index, temp2);
	    					ArrayList<String> t = new ArrayList<String>();
	    					t.add(rel);
	    					rels.put(String.valueOf(sub_index)+","+String.valueOf(obj_index),t);
	    					System.out.println(t);
	    				}
	    			}
	    			else
	    			{
	    				System.out.println("4");
	    				np_count+=1;
	    				np.put(obj,np_count);
	    				temp.add(np_count);
	    				a_list.put(sub_index, temp);
	    				a_list.put(np_count, new HashSet<Integer>() {{add(sub_index);}});
	    				ArrayList<String> t = new ArrayList<String>();
    					t.add(rel);
	    				rels.put(String.valueOf(sub_index)+","+String.valueOf(np_count),t);
	    			}
	    		}
	    		else
	    		{
		    		  np_count+=1;
	    			  np.put(sub, np_count);
	    			  int np_index=np_count;
	    			  ArrayList<String> t3 = new ArrayList<String>();
    				  t3.add(rel);
		    		  if (np.containsKey(obj))
		    		  {
		    			  System.out.println("5");
		    			  int obj_index=np.get(obj);
		    			  a_list.put(np_count,new HashSet<Integer>() {{add(obj_index);}});
		    			  Set<Integer> temp=a_list.get(obj_index);
	    				  temp.add(np_count);
	    				  a_list.put(obj_index,temp);
	    				  rels.put(String.valueOf(obj_index)+","+String.valueOf(np_count), t3);
	    				  System.out.println(t3);
		    		  }
		    		  else
		    		  {
		    			System.out.println("6");
		    			np_count+=1;
		    			np.put(obj, np_count);
		    			int o=np_count;
		    			a_list.put(np_count,new HashSet<Integer>() {{add(np_index);}});
		    			a_list.put(np_index,new HashSet<Integer>() {{add(o);}});
		    			rels.put(String.valueOf(np_index)+","+String.valueOf(np_count), t3);
		    			System.out.println(t3);
		    		  }
		    	
		
		    	}
	    		if(npvpnp.containsKey(sub))
	    		{
	    			ArrayList<String> temp=npvpnp.get(sub);
	    			temp.add("\""+rel+"\"->\""+obj+"\"");
	    			npvpnp.remove(sub);
	    			npvpnp.put(sub, temp);
	    		}
	    		else
	    		{
	    			ArrayList<String> temp=new ArrayList<String>();
	    			temp.add("\""+rel+"\"->\""+obj+"\"");
	    			npvpnp.put(sub,temp);
	    		}
	    	}
	    	i+=1;
	    }
	}

}
