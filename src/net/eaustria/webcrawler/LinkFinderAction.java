/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.eaustria.webcrawler;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import javax.swing.*;

/**
 *
 * @author bmayr
 */

// Recursive Action for forkJoinFramework from Java7

public class LinkFinderAction extends RecursiveAction {

    private String url;
    private ILinkHandler cr;
    LinkFinder lf;
    /**
     * Used for statistics
     */
    private static final long t0 = System.nanoTime();

    public LinkFinderAction(String url, ILinkHandler cr) {
        this.url = url;
        this.cr = cr;
    }

    @Override
    public void compute() {

        List<LinkFinderAction> links = new ArrayList<>();

        // 1. if crawler has not visited url yet:
        if(cr.size() >= 500){
            return;
        }else{
            lf = new LinkFinder(url, cr);
            lf.run();
            for(int i= 0; i < cr.size(); i++){
                LinkFinderAction lfa = new LinkFinderAction(url, cr);
                links.add(lfa);
                cr.addVisited(url);
            }
            invokeAll(links);
        }


        Parser parser;
        NodeFilter filter;
        NodeList list;

        if (0 >= cr.size())
        {
            url = (String) JOptionPane.showInputDialog (
                    null,
                    "Enter the URL to extract links from:",
                    "Web Site",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "http://htmlparser.sourceforge.net/wiki/");
            if (null == url)
                System.exit (1);
        }
        else
            url = cr.toString();
        filter = new NodeClassFilter (LinkTag.class);

        if ((1 < cr.size()) && cr.toString().equalsIgnoreCase ("-maillinks"))
            filter = new AndFilter(
                    filter,
                    new NodeFilter ()
                    {
                        @Override
                        public boolean accept (Node node)
                        {
                            return (((LinkTag)node).isMailLink ());
                        }
                    }
            );
        try
        {
            parser = new Parser (url);
            list = parser.extractAllNodesThatMatch (filter);
            LinkedList<String> lList = new LinkedList<>();
            for (int i = 0; i < list.size (); i++)

                lList.add(list.elementAt (i).toHtml ());
                //System.out.println (list.elementAt (i).toHtml ());
        }
        catch (ParserException e)
        {
            e.printStackTrace ();
        }
        System.exit (0);
        // 2. Create new list of recursiveActions
        // 3. Parse url
        // 4. extract all links from url
        // 5. add new Action for each sublink
        // 6. if size of crawler exceeds 500 -> print elapsed time for statistics
        // -> Do not forget to call ìnvokeAll on the actions!      
    }
}

