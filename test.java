import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Put the tree into tree variable
		Properties props = PropertiesUtils.asProperties("annotators", "tokenize,ssplit,pos,depparse,parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation;
		annotation = new Annotation("Chrysotile is one of the six types of asbestos, and it is the only one that can still be commercialized in many countries. Exposure to other types of asbestos has been associated with serious diseases, such as lung carcinomas and pleural mesotheliomas. The association of chrysotile exposure with disease is controversial. However, <i>in vitro</i> studies show the mutagenic potential of chrysotile, which can induce DNA and cell damage. The present work aimed to analyze alterations in lung small cell carcinoma cultures after 48 h of chrysotile exposure, followed by 2, 4 and 8 days of recovery in fiber-free culture medium. Some alterations, such as aneuploid cell formation, increased number of cells in G2/M phase and cells in multipolar mitosis were observed even after 8 days of recovery. The presence of chrysotile fibers in the cell cultures was detected and cell morphology was observed by laser scanning confocal microscopy. After 4 and 8 days of recovery, only a few chrysotile fragments were present in some cells, and the cellular morphology was similar to that of control cells. Cells transfected with the GFP-tagged α-tubulin plasmid were treated with chrysotile for 24 or 48 h and cells in multipolar mitosis were observed by time-lapse microscopy. Fates of these cells were established: retention in metaphase, cell death, progression through M phase generating more than two daughter cells or cell fusion during telophase or cytokinesis. Some of them were related to the formation of aneuploid cells and cells with abnormal number of centrosomes.");
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for(CoreMap sentence:sentences)
		{
			System.out.println(sentence.toString());
			Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
			tree.pennPrint();
			ArrayList<Tree> np1l=new ArrayList<Tree>(),vpl=new ArrayList<Tree>(),np2l=new ArrayList<Tree>();
			HashMap<Tree,Boolean> n1=new HashMap<Tree,Boolean>(),n2=new HashMap<Tree,Boolean>();
			String tempvp="",tempnp="";
			for(Tree subtree:tree)
			{
				if(subtree.label().value().equals("NP"))
				{
					if(!subtree.getChildrenAsList().contains(Tree.valueOf("NP")))
					{
						if(!np1l.isEmpty())
						{
							if(tree.depth(subtree)<=tree.depth(np1l.get(np1l.size()-1)))
							{
								for(Tree i:np1l)
								{
									for(Tree j:vpl)
									{
										for(Tree k:np2l)
										{
											System.out.println(i.getLeaves()+"##"+j.getLeaves()+"##"+k.getLeaves());
										}
									}
								}
								np1l.clear();
								vpl.clear();
								np2l.clear();
							}
							else
							{
								np2l.add(subtree);
							}
						}
						else if(np1l.isEmpty()||vpl.isEmpty())
						{
							if(!n1.containsKey(subtree))
							{
								np1l.add(subtree);
								for(Tree h:subtree)
								{
									n1.put(h, true);
								}
							}
						}
						else
						{
							if(!n2.containsKey(subtree))
							{
								np2l.add(subtree);
								for(Tree h:subtree)
								{
									n2.put(h, true);
								}
							}
						}
					}
				}
				else if(subtree.label().value().matches("VB.*"))
				{
					if(!vpl.isEmpty())
					{
						for(Tree i:np1l)
						{
							for(Tree j:vpl)
							{
								for(Tree k:np2l)
								{
									System.out.println(i.getLeaves()+"##"+j.getLeaves()+"##"+k.getLeaves());
								}
							}
						}
						vpl.clear();
						np2l.clear();
					}
					vpl.add(subtree);
				}
			}
			for(Tree i:np1l)
			{
				for(Tree j:vpl)
				{
					for(Tree k:np2l)
					{
						System.out.println(i.getLeaves()+"##"+j.getLeaves()+"##"+k.getLeaves());
					}
				}
			}
		}
//	    SemanticGraph graph = SemanticGraphFactory.generateUncollapsedDependencies(tree);
//	    TreebankLangParserParams params = new EnglishTreebankParserParams();
//	    GrammaticalStructureFactory gsf = params.treebankLanguagePack().grammaticalStructureFactory(params.treebankLanguagePack().punctuationWordRejectFilter(), params.typedDependencyHeadFinder());
//	    GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
//	    graph.prettyPrint();
//	    System.out.println(gs.typedDependencies());
//	    SemgrexPattern semgrex = SemgrexPattern.compile("{$}=B >>/.subj.*/ {tag:/NN.*/}=A >>/.obj/ {tag:/NN.*/}=C"),semgrex1 = SemgrexPattern.compile("{$}=B >>/.subj.*/ {}=A >> {tag:/VB.*/}=C");
//	    SemgrexMatcher matcher = semgrex.matcher(graph),matcher1 = semgrex1.matcher(graph);
//	    while (matcher.find()) {
//	      System.out.println(matcher.getNode("A") + " " + matcher.getNode("B")+" "+matcher.getNode("C"));
//	    }
//	    while (matcher1.find()) {
//		      System.out.println(matcher1.getNode("A") + " " + matcher1.getNode("B")+" "+matcher1.getNode("C"));
//		}
	}

}
