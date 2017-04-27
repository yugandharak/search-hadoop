
/* Name - Yugandhara Kulkarni
 * Email - ykulkarn@uncc.edu
 * This program calculates the number of occurrences of each word in input file in the form of logarithmic values.*/

import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

    public class TermFrequency extends Configured implements Tool {

       private static final Logger LOG = Logger .getLogger( TermFrequency.class);

       public static void main( String[] args) throws  Exception {
    	  
    	  //To run toolrunner for the class
          int res  = ToolRunner .run( new TermFrequency(), args);

          System .exit(res);
       }     

       public int run( String[] args) throws  Exception {
    	  
    	  //to create job
          Job job  = Job .getInstance(getConf(), " termfrequency ");
          
          //to set jar
          job.setJarByClass( this .getClass());
          
          //to add path for input file
          FileInputFormat.addInputPaths(job, args[0]);
          
          //to add path for output file
          FileOutputFormat.setOutputPath(job,  new Path(args[ 1]));
          
          //to set mapper
          job.setMapperClass( Map .class);
          
          //to set reducer
          job.setReducerClass( Reduce .class);
          
          //to set data type of key 
          job.setOutputKeyClass( Text .class);
          
          //to set data type of value
          job.setOutputValueClass( DoubleWritable .class);

          return job.waitForCompletion( true)  ? 0 : 1;
       }
      
       public static class Map extends Mapper<LongWritable ,  Text ,  Text ,  DoubleWritable > {
    	   
    	  //to define necessary variables 
          private final static DoubleWritable one  = new DoubleWritable( 1);
          String delimeter=new String("#####");       
          private static final Pattern WordPattern = Pattern .compile("\\s*\\b\\s*");
       
          //Map function which will generate intermediate key value pairs      
          public void map( LongWritable offset,  Text lineText,  Context context)throws  IOException,  InterruptedException {
           
        	 //to retrieve the name of the file from context
             FileSplit fileSplitObj = (FileSplit)context.getInputSplit();
             String fileName = fileSplitObj.getPath().getName();      
            
             //to store current word in a variable
             String line  = lineText.toString().toLowerCase();
             Text currentWordInJob  = new Text();
             String outputString;
             for ( String word  : WordPattern .split(line)) {
                if (word.isEmpty()) {
                   continue;
                }
                outputString= word.toString()+delimeter+fileName;
                currentWordInJob  = new Text(outputString);
                context.write(currentWordInJob,one);
               
             }
          }
       }

       public static class Reduce extends Reducer<Text ,  DoubleWritable ,  Text ,  DoubleWritable > {
    	 //Reducer that takes intermediate key value pairs from map as an input to generate word count with file name.
          @Override
          public void reduce( Text word,  Iterable<DoubleWritable > counts,  Context context)
             throws IOException,  InterruptedException {
             int sum  = 0;
             double LogResult=0;
             for ( DoubleWritable count : counts) {
                sum  += count.get();               
             }
             //code to generate logarithmic value for word count
             LogResult=(Double)(1+(Math.log(sum)/Math.log(10)));
             context.write(word,  new DoubleWritable(LogResult));
          }
       }
    }