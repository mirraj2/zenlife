package zenlife;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;
import jasonlib.IO;
import jasonlib.Json;
import jasonlib.Log;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class QuestionTree {

  Map<Integer, Question> idQuestionMap = Maps.newHashMap();

  public void analyzeTree() throws Exception {
    List<Question> questions = parseQuestions();
    List<Question> rootQuestions = getRootQuestions(questions);

    for (Question q : questions) {
      idQuestionMap.put(q.id, q);
    }

    for (Question question : questions) {
      for (String link : question.links) {
        for (Integer val : parseInts(link)) {
          Question next = idQuestionMap.get(val);
          checkState(next != null, "couldn't find: " + val);
          question.nextQuestions.add(next);
        }
      }
    }

    // printTree(rootQuestions);
    convertToJson(rootQuestions);
  }

  public void convertToJson(List<Question> rootQuestions) {
    Json json = Json.object();

    Json questionsArray = Json.array();

    for (Question question : rootQuestions) {
      questionsArray.add(toJson(question));
    }

    json.with("questions", questionsArray);

    Log.debug(json);
  }

  private Json toJson(Question q) {
    Json ret = Json.object();
    ret.with("id", q.id);
    ret.with("text", q.text);

    Json choices = Json.array();

    for (int i = 0; i < q.choices.size(); i++) {
      Json choice = Json.object();

      choice.with("text", q.choices.get(i));

      String link = q.links.get(i);
      if (!link.isEmpty()) {
        choice.with("links", parseLinks(link));
      }

      choices.add(choice);
    }

    ret.with("choices", choices);

    return ret;
  }

  private Json parseLinks(String link) {
    Json ret = Json.array();

    link = link.toLowerCase();
    link = link.replace("\"", "");
    link = link.replace("if f ", "f=");
    link = link.replace("if m ", "m=");

    try {
      int singleQuestion = parseInt(link);
      Json obj = Json.object();
      obj.with("question", toJson(idQuestionMap.get(singleQuestion)));
      ret.add(obj);
      return ret;
    } catch (Exception e) {
    }

    try {
      for (String s : Splitter.on(',').trimResults().split(link)) {
        Json obj = Json.object();
        if (s.contains("=")) {
          boolean male = s.startsWith("m");
          int questionId = parseInt(s.substring(s.indexOf('=') + 1));
          obj.with("question", toJson(idQuestionMap.get(questionId)));
          obj.with("condition", Json.object().with("sex", male ? "male" : "female"));
        } else {
          obj.with("question", toJson(idQuestionMap.get(parseInt(s))));
        }
        ret.add(obj);
      }
      return ret;
    } catch (Exception e) {
    }

    throw new RuntimeException("Don't know how to handle: " + link);
  }

  public void printTree(List<Question> rootQuestions) {
    for (Question q : rootQuestions) {
      recurse(q, 0);
    }
  }

  private void recurse(Question q, int depth) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      sb.append('\t');
    }
    sb.append(q.toString());
    Log.debug(sb);

    for (Question next : q.nextQuestions) {
      recurse(next, depth + 1);
    }
  }

  private List<Question> getRootQuestions(List<Question> questions) {
    Set<Integer> linked = Sets.newHashSet();

    for (Question question : questions) {
      for (int i = 0; i < question.choices.size(); i++) {
        String link = question.links.get(i);

        if (!link.isEmpty()) {
          for (Integer val : parseInts(link)) {
            linked.add(val);
          }
        }
      }
    }

    List<Question> rootQuestions = Lists.newArrayList();
    for (Question question : questions) {
      if (!linked.contains(question.id)) {
        rootQuestions.add(question);
      }
    }

    return rootQuestions;
  }

  private List<Integer> parseInts(String link) {
    if (link.trim().isEmpty()) {
      return ImmutableList.of();
    }

    link = link.toLowerCase();

    if (link.contains("not used")) {
      return ImmutableList.of();
    }

    if (link.contains("and")) {
      link = link.substring(link.indexOf(',') + 1);
    }

    link = link.replace("\"", "");
    link = link.replace("f=", "");
    link = link.replace("if ", "");
    link = link.replace("f ", "");
    link = link.replace("m ", "");
    link = link.replace("m=", "");
    link = link.replace("=no", "");
    link = link.replace("=yes", "");
    link = link.replace("and", ",");

    List<Integer> ret = Lists.newArrayList();
    if (link.contains(",")) {
      for (String s : Splitter.on(',').trimResults().split(link)) {
        ret.add(parseInt(s));
      }
    } else {
      ret.add(parseInt(link.trim()));
    }
    return ret;
  }

  public List<Question> parseQuestions() throws Exception {
    List<Question> ret = Lists.newArrayList();

    String csv = IO.from(getClass(), "data/question-tree.csv").toString();

    CSVReader reader = new CSVReader(new StringReader(csv));
    reader.readNext();

    String[] row = reader.readNext();
    outer: while (row != null) {
      Question question = new Question(row[0], row[2]);
      ret.add(question);

      while (true) {
        row = reader.readNext();
        if (row == null) {
          break outer;
        }
        if (row[0].trim().isEmpty()) {
          break;
        }
        String answer = row[0];
        String link = row[1];

        question.choices.add(answer);
        question.links.add(link);
      }

      row = reader.readNext();
    }

    reader.close();

    return ret;
  }

  private static class Question {
    public final int id;
    public final String text;
    public final List<String> choices = Lists.newArrayList();
    public final List<String> links = Lists.newArrayList();
    public final List<Question> nextQuestions = Lists.newArrayList();

    public Question(String s, String newText) {
      int i = s.indexOf('.');
      id = Integer.parseInt(s.substring(0, i));

      if (newText.isEmpty()) {
        text = s.substring(i + 2);
      } else {
        text = newText;
      }
    }

    @Override
    public String toString() {
      return id + ". " + text;
    }
  }

  public static void main(String[] args) throws Exception {
    new QuestionTree().analyzeTree();
  }

}
