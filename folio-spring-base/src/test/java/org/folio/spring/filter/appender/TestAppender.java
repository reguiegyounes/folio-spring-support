package org.folio.spring.filter.appender;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

@Plugin(name = "TestAppender", category = "Core", elementType = "appender", printObject = true)
public class TestAppender extends AbstractAppender {

  @Getter
  private final List<LogEvent> messages = new ArrayList<>();

  protected TestAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
    super(name, filter, layout, true, Property.EMPTY_ARRAY);
  }

  @PluginFactory
  public static TestAppender createAppender(@PluginAttribute("name") String name,
                                            @PluginElement("Layout") Layout<? extends Serializable> layout,
                                            @PluginElement("Filter") final Filter filter,
                                            @PluginAttribute("otherAttribute") String otherAttribute) {
    if (name == null) {
      LOGGER.error("No name provided for TestAppender");
      return null;
    }
    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }
    return new TestAppender(name, filter, layout);
  }

  public void clearMessages() {
    messages.clear();
  }

  @Override
  public void append(LogEvent event) {
    messages.add(event);
  }
}
