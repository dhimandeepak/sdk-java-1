/*
* Copyright 2003-2014 CyberSource Corporation
*
* THE SOFTWARE AND THE DOCUMENTATION ARE PROVIDED ON AN "AS IS" AND "AS
* AVAILABLE" BASIS WITH NO WARRANTY.  YOU AGREE THAT YOUR USE OF THE SOFTWARE AND THE
* DOCUMENTATION IS AT YOUR SOLE RISK AND YOU ARE SOLELY RESPONSIBLE FOR ANY DAMAGE TO YOUR
* COMPUTER SYSTEM OR OTHER DEVICE OR LOSS OF DATA THAT RESULTS FROM SUCH USE. TO THE FULLEST
* EXTENT PERMISSIBLE UNDER APPLICABLE LAW, CYBERSOURCE AND ITS AFFILIATES EXPRESSLY DISCLAIM ALL
* WARRANTIES OF ANY KIND, EXPRESS OR IMPLIED, WITH RESPECT TO THE SOFTWARE AND THE
* DOCUMENTATION, INCLUDING ALL WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
* SATISFACTORY QUALITY, ACCURACY, TITLE AND NON-INFRINGEMENT, AND ANY WARRANTIES THAT MAY ARISE
* OUT OF COURSE OF PERFORMANCE, COURSE OF DEALING OR USAGE OF TRADE.  NEITHER CYBERSOURCE NOR
* ITS AFFILIATES WARRANT THAT THE FUNCTIONS OR INFORMATION CONTAINED IN THE SOFTWARE OR THE
* DOCUMENTATION WILL MEET ANY REQUIREMENTS OR NEEDS YOU MAY HAVE, OR THAT THE SOFTWARE OR
* DOCUMENTATION WILL OPERATE ERROR FREE, OR THAT THE SOFTWARE OR DOCUMENTATION IS COMPATIBLE
* WITH ANY PARTICULAR OPERATING SYSTEM.
*/

package com.cybersource.ws.client;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Class containing runTransaction() methods that accept the requests in the
 * form of a Map object.
 */
public class Client {
    private static final String HEADER_FORMAT = "{0}={1}";

    private static final String SOAP_ENVELOPE1 = "<soap:Envelope xmlns:soap=\"" +
            "http://schemas.xmlsoap.org/soap/envelope/\">\n<soap:Body id=\"body1\">\n" +
            "<nvpRequest xmlns=\"{0}\">\n{1}</nvpRequest>" +
            "\n</soap:Body>\n</soap:Envelope>";

    private static final String ELEM_NVP_REPLY = "nvpReply";

    private static final String MERCHANT_ID = "merchantID";

    

    /**
     * Runs a transaction.
     *
     * @param request request to send.
     * @param props   properties the client needs to run the transaction.
     *                See README for more information.
     * @throws FaultException  if a fault occurs.
     * @throws ClientException if any other exception occurs.
     */
    public static Map runTransaction(Map<String,String> request, Properties props)
            throws FaultException, ClientException {
        return (runTransaction(
                request, props, null, true, true));
    }

    /**
     * Runs a transaction.
     *
     * @param request      request to send.
     * @param props        properties the client needs to run the transaction.
     *                     See README for more information.
     * @param _logger      Logger object to used for logging.
     * @param prepare      Flag as to whether or not the logger's
     *                     prepare() method should be called.
     * @param logTranStart Flag as to whether or not the logger's
     *                     logTransactionStart() method should be called.
     * @throws FaultException  if a fault occurs.
     * @throws ClientException if any other exception occurs.
     */
    public static Map runTransaction(
            Map<String, String> request, Properties props,
            Logger _logger, boolean prepare, boolean logTranStart)
            throws FaultException, ClientException {
        MerchantConfig mc;
        LoggerWrapper logger = null;
        Connection con = null;

        try {
            setVersionInformation(request);

            String merchantID = request.get(MERCHANT_ID);
            if (merchantID == null) {
                // if no merchantID is present in the request, get its
                // value from the properties and add it to the request.
                mc = new MerchantConfig(props, null);
                merchantID = mc.getMerchantID();
                request.put(MERCHANT_ID, merchantID);
            } else {
                mc = new MerchantConfig(props, merchantID);
            }

            logger = new LoggerWrapper(_logger, prepare, logTranStart, mc);

            DocumentBuilder builder = Utility.newDocumentBuilder();

            Document signedDoc
                    = soapWrapAndSign(request, mc, logger);

            con = Connection.getInstance(mc, builder, logger);
            Document wrappedReply = con.post(signedDoc);

            return (soapUnwrap(wrappedReply, mc, logger));
        } catch (IOException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (ParserConfigurationException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (SignException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } catch (ConfigException e) {
            throw new ClientException(
                    e, con != null && con.isRequestSent(), logger);
        } finally {
            if (con != null) {
                con.release();
            }
        }
    }

    /**
     * Sets the version information in the request.
     *
     * @param request request to set the version information in.
     */
    private static void setVersionInformation(Map<String, String> request) {
        request.put("clientLibrary", "Java Basic");
        request.put("clientLibraryVersion", Utility.VERSION);
        request.put("clientEnvironment", Utility.ENVIRONMENT);
    }


    /**
     * Wraps the given Map object in SOAP envelope and signs it.
     *
     * @param request Map object containing the request.
     * @param mc      MerchantConfig object.
     * @param logger  LoggerWrapper object to use for logging.
     * @return signed document.
     * @throws IOException   if reading from string fails.
     * @throws SignException if signing fails.
     */
    private static Document soapWrapAndSign(
            Map request, MerchantConfig mc,
            LoggerWrapper logger)
            throws
            IOException, SignException {
        boolean logSignedData = mc.getLogSignedData();
        if (!logSignedData) {
            logger.log(
                    Logger.LT_REQUEST,
                    mapToString(request, true, PCI.REQUEST));
        }
        Document doc;

        // wrap in SOAP envelope
        Object[] arguments
                = {mc.getEffectiveNamespaceURI(),
                mapToString(request, false, PCI.REQUEST)};
        String xmlString = MessageFormat.format(SOAP_ENVELOPE1, arguments);
        doc = ApacheSignatureWrapper.soapWrapAndSign(xmlString, mc, logger);

        if (logSignedData) {
            logger.log(Logger.LT_REQUEST,
                    Utility.nodeToString(doc, PCI.REQUEST));
        }
        return doc;
    }


    /**
     * Extracts the content of the SOAP body from the given Document object
     * inside a SOAP envelope.
     *
     * @param doc    Document object to extract content from.
     * @param mc     MerchantConfig object.
     * @param logger LoggerWrapper object to use for logging.
     * @return content of SOAP body as a Map object.
     */
    private static HashMap soapUnwrap(
            Document doc, MerchantConfig mc, LoggerWrapper logger) {
        boolean logSignedData = mc.getLogSignedData();
        if (logSignedData) {
            logger.log(Logger.LT_REPLY,
                    Utility.nodeToString(doc, PCI.REPLY));
        }

        // look for the nvpReply element
        Node nvpReply
                = Utility.getElement(
                doc, ELEM_NVP_REPLY, mc.getEffectiveNamespaceURI());

        Text nvpString = (Text) nvpReply.getFirstChild();

        String replyString = nvpString.getNodeValue();

        HashMap reply = Utility.stringToMap(replyString);

        if (!logSignedData) {
            logger.log(
                    Logger.LT_REPLY,
                    mapToString(reply, true, PCI.REPLY));
        }

        return reply;
    }

    /**
     * Converts the contents of a Map object into a string, one name-value pair
     * to a line and the name and value are separated by an equal sign.
     *
     * @param src  Map object whose contents are being converted.
     * @param mask Flag whether or not to mask "unsafe" fields.
     * @param type Relevant only when mask is true, indicates whether this
     *             is the request or the reply map.  Pass either
     *             PCI.REQUEST or PCI.REPLY.
     * @return resulting string; will be empty if the Map object was null or
     *         empty.
     */
    static String mapToString(Map src, boolean mask, int type) {
       return Utility.mapToString(src, mask, type);
    }

    
} 

