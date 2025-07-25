import matcher.Matchable;
import matcher.Matcher;

/**
 * description
 *
 * @author <a href="mailto: panoshu@gmail.com">panoshu</a>
 * @version 1.0
 * @since 2025/7/18 15:28
 */

public class ProcessDefinition implements Matchable {
  String id;
  String inheritsFrom;
  boolean isAbstract;
  Matcher matcher;
  List<StepDefinition> steps;
}
