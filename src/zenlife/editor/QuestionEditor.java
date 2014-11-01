package zenlife.editor;

import jasonlib.IO;
import jasonlib.Json;
import jasonlib.swing.component.GFrame;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import zenlife.RService;

public class QuestionEditor extends JComponent {

  private final Json model;
  private final JTabbedPane tabs = new JTabbedPane();

  public QuestionEditor() {
    model = IO.from(RService.class, "data/questions.json").toJson();
    for (Json section : model.asJsonArray()) {
      tabs.addTab(section.get("title"), new Section(this, section));
    }

    setLayout(new MigLayout("insets 10, gap 10"));
    add(tabs, "width 100%, height 100%");
  }

  public void updateTitles() {
    for (int i = 0; i < tabs.getTabCount(); i++) {
      Section section = (Section) tabs.getComponent(i);
      tabs.setTitleAt(i, section.getTitle());
    }
  }

  public int addSection(String name) {
    Json sectionJson = Json.object().with("title", name).with("questions", Json.array());
    model.add(sectionJson);
    tabs.addTab(name, new Section(this, sectionJson));
    return tabs.getTabCount() - 1;
  }

  public JTabbedPane getTabs() {
    return tabs;
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        GFrame.create().title("ZenLife Question Editor").content(new QuestionEditor()).size(1000, 700).start();
      }
    });
  }

}
