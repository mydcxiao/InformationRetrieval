import java.io.IOException;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Bigram {
   public static class TokenizerMapper extends Mapper<Object, Text, Text, Text>
   {
      private Text word = new Text();
      private Text docID = new Text();
      private final HashSet<String> TARGET = new HashSet<String>(Arrays.asList("computer science", "information retrieval", "power politics", "los angeles", "bruce willis"));

      public void map(Object key, Text value, Context context) throws IOException, InterruptedException 
      {
         String[] doc = value.toString().split("\t", 2);
         String content = doc[1].toLowerCase().replaceAll("[^a-z]+", " ");
         docID.set(doc[0]);
         StringTokenizer itr = new StringTokenizer(content);
         String first = itr.nextToken();
         while (itr.hasMoreTokens()) 
         {
            String second = itr.nextToken();
            String bigram = first + " " + second;
            if (TARGET.contains(bigram))
            {
               word.set(bigram);
               context.write(word, docID);     
            }
            first = second;
         }
      }
   }

   public static class IndexReducer extends Reducer<Text,Text,Text,Text> 
   {
      private Text result = new Text();
      public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException 
      {
         HashMap<String, Integer> invertedIdx = new HashMap<>();
         for(Text val : values)
           {
             String docID = val.toString();
             invertedIdx.put(docID, invertedIdx.getOrDefault(docID, 0) + 1);
           }
         StringBuilder sb = new StringBuilder();
         invertedIdx.forEach((k, v) -> {
           sb.append(k).append(":").append(v).append(" ");
         });
        
         result.set(sb.deleteCharAt(sb.length()-1).toString());
         context.write(key, result);
      }
   }

   public static void main(String[] args) throws Exception 
   {
      Configuration conf = new Configuration();
      Job job = Job.getInstance(conf, "Bigram");

      job.setJarByClass(Bigram.class);
      job.setMapperClass(TokenizerMapper.class);
      // job.setCombinerClass(IndexReducer.class);
      job.setReducerClass(IndexReducer.class);

      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(Text.class);

      FileInputFormat.addInputPath(job, new Path(args[0]));
      FileOutputFormat.setOutputPath(job, new Path(args[1]));

      System.exit(job.waitForCompletion(true) ? 0 : 1);
   }
}// Bigram

