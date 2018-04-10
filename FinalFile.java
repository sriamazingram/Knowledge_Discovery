import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;
public class FinalFile {
	public static ArrayList<String> subs=new ArrayList<String>(),relas=new ArrayList<String>(),objs=new ArrayList<String>();
	public static Map<String,Integer> np = new HashMap<String,Integer>();
	public static Map<String,ArrayList<String>> rels = new HashMap<String,ArrayList<String>>();
	public static Map<Integer,Set<Integer>> a_list = new HashMap<Integer,Set<Integer>>();
	public static int np_count=0;
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException{
		PrintWriter writer = new PrintWriter("/home/sriamazingram/Scientific_Articles/Results/climate_triplets");
		JSONParser parser = new JSONParser();
		Object object = parser.parse(new FileReader("/home/sriamazingram/Scientific_Articles/Dataset/climate.json"));
		JSONObject jsonObject = (JSONObject)object;
		JSONObject response = (JSONObject)jsonObject.get("response");
		JSONArray docs = (JSONArray)response.get("docs");
		PrintWriter out;
	    out = new PrintWriter(System.out);
	    Properties props = PropertiesUtils.asProperties("annotators", "tokenize,ssplit,pos,depparse,parse");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    for(int j=0;j<docs.size();j++)
	    {
	    	System.out.println(j+1);
	    	JSONObject jsonObj = (JSONObject)docs.get(j);
	    	String content=(String)jsonObj.get("content");
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
	    	Annotation annotation = new Annotation(content);
		    pipeline.annotate(annotation);
		    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		    String np1= "";
		    ArrayList<String> np2= new ArrayList<String>();
		    String vp= "";
		    HashMap <Tree,Boolean> map=new HashMap <Tree,Boolean>();
		    for(CoreMap sentence:sentences){
		      Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
		      
		      for (Tree t:tree)
		      {
		    	  if(map.containsKey(t))
					  continue;
		    	  if(t.value().equals("NP"))
		    	  {
		    		  
		    		  if(!np1.isEmpty()) 
		    		  {
		    			  for(String x: np2)
		    			  {
		    				  //System.out.println(np1+"/"+vp+"/"+x);
			    			  subs.add(np1.trim());
			    			  relas.add(vp.trim());
			    			  objs.add(x.trim());
			    			  writer.println(np1.trim()+"##"+vp.trim()+"##"+x.trim());
		    			  }
		    			  np2.clear();
		    			  np1="";
		    			  vp="";
		    			  map=new HashMap <Tree,Boolean>();
		    			  
		    			  
		    		  }
		    		  for(Tree d:t.getLeaves())
		    		  {
		    			  np1+=d.toString()+" ";
		    		  }
		    		  //np1=t.getLeaves().toString();  
		    		  for(Tree x: t)
		    		  {
		    			  if(x.value().equals("NP"))
		    				  map.put(x, true);
		    			  
		    		  }
		    		  map.put(t, true);
		    		  
		    		  
		    	  }
		    	 if(t.value().equals("VP"))
		    	 {
		    		List<Label> preterm= t.preTerminalYield();
		 			List<Tree> leaves= t.getLeaves() ; 
		 			vp+=leaves.get(0).toString()+" ";
		
		 			if(preterm.size()>1)
		 			{
		 				for(int i=1;i<leaves.size();i++)
		 				{
		 					
		 					if(!(preterm.get(i).toString().matches("V.*")))
		 					{
		 						if((preterm.get(i-1).toString().matches("V.*"))) {
		 							break;
		 						}
		 					}
		 					vp+=(String)leaves.get(i).toString()+" ";
		 				}
		 				
		 			}
		    		 for(Tree x:t)
		    		 {
		    			 if(map.containsKey(x))
		    			 {
		    				 continue;
		    			 }
		    			if(x.value().equals("NP"))
		    			{
		    				String s="";
		    				for(Tree d:x.getLeaves())
		    	    		{
		    	    			  s+=d.toString()+" ";
		    	    		}
		    				np2.add(s);
		    				for(Tree y: x)
		    				{
		    					map.put(y,true);
		    				}
		    				map.put(x,true);
		    			}
		    			else if(x.value().equals("VP"))
		    				map.put(x, true);
		    		
		    	     }
		    		 for(String x: np2)
		   			 {
		    			 //System.out.println(np1+"/"+vp+"/"+x);
		    			 subs.add(np1.trim());
		    			  relas.add(vp.trim());
		    			  objs.add(x.trim());
		    			  writer.println(np1.trim()+"##"+vp.trim()+"##"+x.trim());
		   			 }
		   			  np2.clear();
		   			  vp="";
		    	 }
		      }
		      if(!np1.isEmpty()) 
			  {
		    	  for(String x: np2)
		   		  {
		    			 //System.out.println(np1+"/"+vp+"/"+x);
		    			 subs.add(np1.trim());
		    			  relas.add(vp.trim());
		    			  objs.add(x.trim());
		    			  writer.println((np1.trim()+"##"+vp.trim()+"##"+x.trim()));
		   		  }
				  np2.clear();
				  np1="";
				  vp="";
				  map=new HashMap <Tree,Boolean>();
			  }
		    }
	    }
	    writer.close();
	    parserFunction();
	    printGraph();
	    IOUtils.closeIgnoringExceptions(out);
	}
	@SuppressWarnings("serial")
	private static void parserFunction() throws FileNotFoundException {
		// TODO Auto-generated method stub
		for(int i=0;i<subs.size();i++)
		{
			String sub=subs.get(i),rel=relas.get(i),obj=objs.get(i);
			if(sub=="" || rel=="" || obj=="" || obj.equals(sub))
	    	  {
	    		  continue;
	    	  }
	    	  String temp_string=rel;
	    	  if(np.containsKey(sub))
	    	  {
	    		  int np_index=np.get(sub);
	    		  Set<Integer> temp=a_list.get(np_index);
	    		  if(np.containsKey(obj))
	    		  {
	    			  int obj_index=np.get(obj);
	    			  if(temp.contains(obj_index))
	    			  {
	    				  ArrayList<String> x;
	    				  if(rels.containsKey(String.valueOf(np_index)+","+String.valueOf(obj_index)))
	    				  {

	    					  x=rels.get(String.valueOf(np_index)+","+String.valueOf(obj_index));
	    					  x.add(rel);
			    			  rels.put(String.valueOf(np_index)+","+String.valueOf(obj_index),x);

	    				  }
	    				  else
	    				  {

	    					  x=rels.get(String.valueOf(obj_index)+","+String.valueOf(np_index));
	    					  x.add(rel);
			    			  rels.put(String.valueOf(obj_index)+","+String.valueOf(np_index),x);
	    				  }

	    			  }
	    			  else {


	    				    temp.add(obj_index);
	    				    a_list.put(np_index,temp);
	    				    temp=a_list.get(obj_index);
	    				    temp.add(np_index);
	    				    a_list.put(obj_index,temp);
	    				    rels.put(String.valueOf(np_index)+","+String.valueOf(obj_index), new ArrayList<String>() {{add(temp_string);}});

	    			  }

	    		  }
	    		  else {

	    			    np_count+=1;
	    			  	np.put(obj, np_count);
	    			  	temp.add(np_count);
  				    a_list.put(np_index,temp);
  				    a_list.put(np_count,new HashSet<Integer>() {{add(np_index);}});
  				    rels.put(String.valueOf(np_index)+","+String.valueOf(np_count), new ArrayList<String>() {{add(temp_string);}});

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

	    			  a_list.put(np_count,new HashSet<Integer>() {{add(obj_index);}});
	    			  Set<Integer> temp=a_list.get(obj_index);
  				  temp.add(np_count);
  				  a_list.put(obj_index,temp);
  				  rels.put(String.valueOf(obj_index)+","+String.valueOf(np_count), new ArrayList<String>() {{add(temp_string);}});

	    		  }
	    		  else
	    		  {

	    			np_count+=1;
	    			np.put(obj, np_count);
	    			a_list.put(np_count,new HashSet<Integer>() {{add(np_index);}});
	    			a_list.put(np_index,new HashSet<Integer>() {{add(np_count);}});
	    			rels.put(String.valueOf(np_index)+","+String.valueOf(np_count), new ArrayList<String>() {{add(temp_string);}});
	    		  }


}
		}
	}
	private static void printGraph() throws FileNotFoundException {
		// TODO Auto-generated method stub
		try {
			PrintWriter writer = new PrintWriter("/home/sriamazingram/Scientific_Articles/Results/graph_climate");
			PrintWriter writer1 = new PrintWriter("/home/sriamazingram/Scientific_Articles/Results/np_climate");
		    PrintWriter writer2 = new PrintWriter("/home/sriamazingram/Scientific_Articles/Results/rel_climate");
		    int i,edges=0;
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
			Process p = Runtime.getRuntime().exec("/home/sriamazingram/Downloads/graclus1.2/graclus graph_climate"+" "+String.valueOf(nodes/10), null, new File("/home/sriamazingram/Scientific_Articles/Results/"));
		      
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
}