package com.sun.org.apache.xml.internal.utils;

import com.sun.org.apache.xml.internal.res.XMLMessages;
import java.io.Writer;
import java.util.Stack;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class DOMBuilder
  implements ContentHandler, LexicalHandler
{
  public Document m_doc;
  protected Node m_currentNode = null;
  protected Node m_root = null;
  protected Node m_nextSibling = null;
  public DocumentFragment m_docFrag = null;
  protected Stack m_elemStack = new Stack();
  protected boolean m_inCData = false;
  
  public DOMBuilder(Document paramDocument, Node paramNode)
  {
    this.m_doc = paramDocument;
    this.m_currentNode = (this.m_root = paramNode);
    if ((paramNode instanceof Element)) {
      this.m_elemStack.push(paramNode);
    }
  }
  
  public DOMBuilder(Document paramDocument, DocumentFragment paramDocumentFragment)
  {
    this.m_doc = paramDocument;
    this.m_docFrag = paramDocumentFragment;
  }
  
  public DOMBuilder(Document paramDocument)
  {
    this.m_doc = paramDocument;
  }
  
  public Node getRootDocument()
  {
    return null != this.m_docFrag ? this.m_docFrag : this.m_doc;
  }
  
  public Node getRootNode()
  {
    return this.m_root;
  }
  
  public Node getCurrentNode()
  {
    return this.m_currentNode;
  }
  
  public void setNextSibling(Node paramNode)
  {
    this.m_nextSibling = paramNode;
  }
  
  public Node getNextSibling()
  {
    return this.m_nextSibling;
  }
  
  public Writer getWriter()
  {
    return null;
  }
  
  protected void append(Node paramNode)
    throws SAXException
  {
    Node localNode = this.m_currentNode;
    if (null != localNode)
    {
      if ((localNode == this.m_root) && (this.m_nextSibling != null)) {
        localNode.insertBefore(paramNode, this.m_nextSibling);
      } else {
        localNode.appendChild(paramNode);
      }
    }
    else if (null != this.m_docFrag)
    {
      if (this.m_nextSibling != null) {
        this.m_docFrag.insertBefore(paramNode, this.m_nextSibling);
      } else {
        this.m_docFrag.appendChild(paramNode);
      }
    }
    else
    {
      int i = 1;
      int j = paramNode.getNodeType();
      if (j == 3)
      {
        String str = paramNode.getNodeValue();
        if ((null != str) && (str.trim().length() > 0)) {
          throw new SAXException(XMLMessages.createXMLMessage("ER_CANT_OUTPUT_TEXT_BEFORE_DOC", null));
        }
        i = 0;
      }
      else if ((j == 1) && (this.m_doc.getDocumentElement() != null))
      {
        i = 0;
        throw new SAXException(XMLMessages.createXMLMessage("ER_CANT_HAVE_MORE_THAN_ONE_ROOT", null));
      }
      if (i != 0) {
        if (this.m_nextSibling != null) {
          this.m_doc.insertBefore(paramNode, this.m_nextSibling);
        } else {
          this.m_doc.appendChild(paramNode);
        }
      }
    }
  }
  
  public void setDocumentLocator(Locator paramLocator) {}
  
  public void startDocument()
    throws SAXException
  {}
  
  public void endDocument()
    throws SAXException
  {}
  
  public void startElement(String paramString1, String paramString2, String paramString3, Attributes paramAttributes)
    throws SAXException
  {
    Element localElement;
    if ((null == paramString1) || (paramString1.length() == 0)) {
      localElement = this.m_doc.createElementNS(null, paramString3);
    } else {
      localElement = this.m_doc.createElementNS(paramString1, paramString3);
    }
    append(localElement);
    try
    {
      int i = paramAttributes.getLength();
      if (0 != i) {
        for (int j = 0; j < i; j++)
        {
          if (paramAttributes.getType(j).equalsIgnoreCase("ID")) {
            setIDAttribute(paramAttributes.getValue(j), localElement);
          }
          String str1 = paramAttributes.getURI(j);
          if ("".equals(str1)) {
            str1 = null;
          }
          String str2 = paramAttributes.getQName(j);
          if ((str2.startsWith("xmlns:")) || (str2.equals("xmlns"))) {
            str1 = "http://www.w3.org/2000/xmlns/";
          }
          localElement.setAttributeNS(str1, str2, paramAttributes.getValue(j));
        }
      }
      this.m_elemStack.push(localElement);
      this.m_currentNode = localElement;
    }
    catch (Exception localException)
    {
      throw new SAXException(localException);
    }
  }
  
  public void endElement(String paramString1, String paramString2, String paramString3)
    throws SAXException
  {
    this.m_elemStack.pop();
    this.m_currentNode = (this.m_elemStack.isEmpty() ? null : (Node)this.m_elemStack.peek());
  }
  
  public void setIDAttribute(String paramString, Element paramElement) {}
  
  public void characters(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException
  {
    if ((isOutsideDocElem()) && (XMLCharacterRecognizer.isWhiteSpace(paramArrayOfChar, paramInt1, paramInt2))) {
      return;
    }
    if (this.m_inCData)
    {
      cdata(paramArrayOfChar, paramInt1, paramInt2);
      return;
    }
    String str = new String(paramArrayOfChar, paramInt1, paramInt2);
    Object localObject = this.m_currentNode != null ? this.m_currentNode.getLastChild() : null;
    if ((localObject != null) && (localObject.getNodeType() == 3))
    {
      ((Text)localObject).appendData(str);
    }
    else
    {
      Text localText = this.m_doc.createTextNode(str);
      append(localText);
    }
  }
  
  public void charactersRaw(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException
  {
    if ((isOutsideDocElem()) && (XMLCharacterRecognizer.isWhiteSpace(paramArrayOfChar, paramInt1, paramInt2))) {
      return;
    }
    String str = new String(paramArrayOfChar, paramInt1, paramInt2);
    append(this.m_doc.createProcessingInstruction("xslt-next-is-raw", "formatter-to-dom"));
    append(this.m_doc.createTextNode(str));
  }
  
  public void startEntity(String paramString)
    throws SAXException
  {}
  
  public void endEntity(String paramString)
    throws SAXException
  {}
  
  public void entityReference(String paramString)
    throws SAXException
  {
    append(this.m_doc.createEntityReference(paramString));
  }
  
  public void ignorableWhitespace(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException
  {
    if (isOutsideDocElem()) {
      return;
    }
    String str = new String(paramArrayOfChar, paramInt1, paramInt2);
    append(this.m_doc.createTextNode(str));
  }
  
  private boolean isOutsideDocElem()
  {
    return (null == this.m_docFrag) && (this.m_elemStack.size() == 0) && ((null == this.m_currentNode) || (this.m_currentNode.getNodeType() == 9));
  }
  
  public void processingInstruction(String paramString1, String paramString2)
    throws SAXException
  {
    append(this.m_doc.createProcessingInstruction(paramString1, paramString2));
  }
  
  public void comment(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException
  {
    append(this.m_doc.createComment(new String(paramArrayOfChar, paramInt1, paramInt2)));
  }
  
  public void startCDATA()
    throws SAXException
  {
    this.m_inCData = true;
    append(this.m_doc.createCDATASection(""));
  }
  
  public void endCDATA()
    throws SAXException
  {
    this.m_inCData = false;
  }
  
  public void cdata(char[] paramArrayOfChar, int paramInt1, int paramInt2)
    throws SAXException
  {
    if ((isOutsideDocElem()) && (XMLCharacterRecognizer.isWhiteSpace(paramArrayOfChar, paramInt1, paramInt2))) {
      return;
    }
    String str = new String(paramArrayOfChar, paramInt1, paramInt2);
    CDATASection localCDATASection = (CDATASection)this.m_currentNode.getLastChild();
    localCDATASection.appendData(str);
  }
  
  public void startDTD(String paramString1, String paramString2, String paramString3)
    throws SAXException
  {}
  
  public void endDTD()
    throws SAXException
  {}
  
  public void startPrefixMapping(String paramString1, String paramString2)
    throws SAXException
  {}
  
  public void endPrefixMapping(String paramString)
    throws SAXException
  {}
  
  public void skippedEntity(String paramString)
    throws SAXException
  {}
}
