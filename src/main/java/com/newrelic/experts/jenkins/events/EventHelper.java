package com.newrelic.experts.jenkins.events;

import com.google.inject.Singleton;

import com.newrelic.experts.client.model.Event;

import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.util.TagCloud;
import hudson.util.VersionNumber;

import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An event producer for {@code AppBuildEvent}s.
 * 
 * @author sdewitt
 */
@Singleton
public class EventHelper {
  
  private static final String CLASS_NAME = EventHelper.class.getName();
  private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
  
  /**
   * Assign any node labels to {@code attributeName} in {@code event}.
   * 
   * @param event the event.
   * @param attributeName the attribute name in which to store the labels.
   * @param node the Jenkins node.
   */
  public void setLabels(Event event, String attributeName, Node node) {
    List<String> labels = new ArrayList<String>();
    
    for (TagCloud<LabelAtom>.Entry atom : node.getLabelCloud()) {
      labels.add(atom.item.getDisplayName());
    }
    
    event.put(attributeName, String.join("|", labels));
  }
  
  /**
   * Assign the node hostname to {@code attributeName} in {@code event}.
   * 
   * @param event the event.
   * @param attributeName the attribute name in which to store the hostname.
   * @param node the Jenkins node.
   */
  public void setHostname(Event event, String attributeName, Node node) {
    Computer computer = node.toComputer();
    if (computer != null) {
      try {
        event.put(attributeName, computer.getHostName());
      } catch (IOException | InterruptedException exc) {
        LOGGER.log(Level.WARNING, String.format(
            "Could not get node hostname: %s",
            exc.getMessage()
        ), exc);
      }
    }
  }
  
  /**
   * Set common Jenkins attributes on the event.
   * 
   * @param event the event.
   */
  public void setCommonAttributes(Event event) {
    Jenkins jenkins = getJenkins();
    VersionNumber ver = Jenkins.getVersion();
    
    event.put("provider", "Jenkins");
    event.put("providerVersion", (ver != null ? ver.toString() : "unknown"));
    setLabels(event, "jenkinsMasterLabels", jenkins);
    setHostname(event, "jenkinsMasterHost", jenkins);
  }
  
  /**
   * Return the global Jenkins instance.
   * 
   * @return the global Jenkins instance.
   */
  @SuppressWarnings("deprecation")
  public Jenkins getJenkins() {
    return Jenkins.getInstance();
  }
}
