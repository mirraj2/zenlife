package zenlife.editor;

import static com.google.common.base.Preconditions.checkState;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.tree.TreeNode;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class Node implements TreeNode, Iterable<Node> {

  private Object value;
  private Node parent;
  private final List<Node> children = Lists.newArrayList();

  public Node() {
    this(null);
  }

  public Node(Object value) {
    this.value = value;
  }

  public void add(Node child) {
    add(child, getChildCount());
  }

  public void add(Node child, int index) {
    if (child.parent != null) {
      child.parent.children.remove(child);
    }
    child.parent = this;

    children.add(index, child);
  }

  public void remove(Node child){
    checkState(children.remove(child));
    child.parent = null;
  }

  public void remove() {
    parent.remove(this);
  }

  @Override
  public Node getChildAt(int index) {
    return children.get(index);
  }

  @Override
  public int getChildCount() {
    return children.size();
  }

  @Override
  public Node getParent() {
    return parent;
  }

  @Override
  public int getIndex(TreeNode node) {
    return children.indexOf(node);
  }

  public int getIndex() {
    return parent.getIndex(this);
  }

  @Override
  public boolean getAllowsChildren() {
    return true;
  }

  @Override
  public boolean isLeaf() {
    return getChildCount() == 0;
  }

  public boolean isRoot() {
    return parent == null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Enumeration children() {
    return Iterators.asEnumeration(children.iterator());
  }

  @Override
  public String toString() {
    return value == null ? "" : value.toString();
  }

  public void setValue(Object value) {
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue() {
    return (T) value;
  }

  public int getDepth() {
    if (isRoot()) {
      return 0;
    }
    return 1 + parent.getDepth();
  }

  @Override
  public Iterator<Node> iterator() {
    return children.iterator();
  }

}
