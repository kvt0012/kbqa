package application;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import log.QueryLogger;

import org.json.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import rdf.Sparql;
import qa.GAnswer;
import qa.Globals;
import qa.Matches;

public class GanswerHandler extends AbstractHandler{

	public static String errorHandle(String status,String message,String question,QueryLogger qlog){
		JSONObject exobj = new JSONObject();
		try {
			exobj.put("status", status);
			exobj.put("message", message);
			exobj.put("question", question);
			if(qlog!=null&&qlog.rankedSparqls!=null&&qlog.rankedSparqls.size()>0){
				exobj.put("sparql", qlog.rankedSparqls.get(0).toStringForGStore2());
			}
		} catch (Exception e1) {
		}
		return exobj.toString();
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
		String question = "";
		String kb = "dbpedia";
		QueryLogger qlog = null;

		long start_time = System.currentTimeMillis();

		try{
			response.setContentType("application/json");
	        response.setStatus(HttpServletResponse.SC_OK);
	        //step1: parsing input json
	        int needAnswer = 0;
	        int needSparql = 1;
	        question = request.getParameter("query");

			if(request.getParameterMap().containsKey("kb")) {
				kb = request.getParameter("kb");
				System.out.println(kb);
				if (!kb.equals("dbpedia")) {
					try {
						baseRequest.setHandled(true);
						response.getWriter().println(errorHandle("500", "InvalidKBException: the KB you input is invalid, please check", kb, qlog));
					} catch (Exception e1) {
					}
					return;
				}
			}

			if(!request.getParameterMap().containsKey("maxAnswerNum")){
				needAnswer = GanswerHttp.maxAnswerNum;
			}
			else{
				needAnswer = Integer.parseInt(request.getParameter("maxAnswerNum"));
			}
			if(!request.getParameterMap().containsKey("maxSparqlNum")){
				needSparql = GanswerHttp.maxSparqlNum;
			}else{
				needSparql = Integer.parseInt(request.getParameter("maxSparqlNum"));
			}
			Globals.MaxAnswerNum = needAnswer;

	        //step2 run GAnswer Logic
	        String input = question;
	        GAnswer ga = new GAnswer();
	        qlog = ga.getSparqlList(input);
	        if(qlog == null || qlog.rankedSparqls == null){
				try {
					baseRequest.setHandled(true);
					response.getWriter().println(errorHandle("500","InvalidQuestionException: the question you input is invalid, please check",question,qlog));
				} catch (Exception e1) {
				}
	        	return;
	        }
	        int idx;

			//step2 construct response
			JSONObject ansobj = new JSONObject();
			JSONObject tmpobj = new JSONObject();
			if(needAnswer > 0){
				boolean isBoolean = false;
				boolean boolAns = false;
				if(qlog!=null && qlog.rankedSparqls.size()!=0){
					Sparql curSpq = null;
					Matches m = null;
					for(idx = 1;idx<=Math.min(qlog.rankedSparqls.size(), 5);idx+=1){
						curSpq = qlog.rankedSparqls.get(idx-1);
						if(curSpq.tripleList.size()>0&&curSpq.questionFocus!=null){
							m = ga.getAnswerFromGStore2(curSpq);
						}
						if(m!=null&&m.answers!=null){
							qlog.sparql = curSpq;
							qlog.match = m;
							break;
						}
					}
					if(m==null||m.answers==null){
						curSpq = ga.getUntypedSparql(curSpq);
						if(curSpq!=null){
							m = ga.getAnswerFromGStore2(curSpq);
						}
						if(m!=null&&m.answers!=null){
							qlog.sparql = curSpq;
							qlog.match = m;
						}
					}
					if(qlog.match==null)
						qlog.match=new Matches();
					if(qlog.sparql==null)
						qlog.sparql = qlog.rankedSparqls.get(0);
					qlog.reviseAnswers();

					//adding variables to result json
					JSONArray vararr = new JSONArray();
					for(String var : qlog.sparql.variables){
						vararr.put(var);
					}
					JSONObject headobj = new JSONObject();
					headobj.put("vars", vararr);
					ansobj.put("head", headobj);

					//adding answers to result json
					JSONArray resultobj = new JSONArray();
					JSONObject bindingobj;
					System.out.println(qlog.match.answersNum);
					for(int i=0;i<qlog.match.answersNum;i++){
						int j = 0;
						bindingobj = new JSONObject();
						for(String var:qlog.sparql.variables){
							JSONObject bidobj = new JSONObject();
							String ansRiv = qlog.match.answers[i][j].substring(qlog.match.answers[i][j].indexOf(":")+1);
							if(ansRiv.startsWith("<")) {
								bidobj.put("type", "uri");
								ansRiv = ansRiv.substring(1, ansRiv.length() - 1);
								bidobj.put("value", "http://dbpedia.org/resource/" + ansRiv);
							}
							else {
								bidobj.put("type", "literal");
								if (ansRiv.startsWith("\"") && ansRiv.endsWith("\"")) {
									ansRiv = ansRiv.substring(1, ansRiv.length() - 1);
								}
								bidobj.put("value", ansRiv);
								if (ansRiv.contains("boolean")
										&& (ansRiv.contains("false") || ansRiv.contains("true"))
										&& qlog.match.answersNum == 1) {
									isBoolean = true;
									boolAns = !ansRiv.contains("false");
								}
							}
							System.out.println(qlog.match.answers[i][j]);
							j += 1;
							bindingobj.put(var, bidobj);
						}
						resultobj.put(bindingobj);
					}
					tmpobj.put("bindings", resultobj);
				}
				if (!isBoolean) {
					ansobj.put("results", tmpobj);
				} else {
					ansobj.put("head", new JSONObject());
					ansobj.put("boolean", boolAns);
				}
			}
			if(needSparql>0){
				JSONArray spqarr = new JSONArray();
				spqarr.put(qlog.sparql.toStringForGStore2());
				for(idx=0;idx<needSparql-1&&idx<qlog.rankedSparqls.size();idx+=1){
					if(qlog.sparql.toStringForGStore2().compareTo(qlog.rankedSparqls.get(idx).toStringForGStore2()) != 0)
						spqarr.put(qlog.rankedSparqls.get(idx).toStringForGStore2());
				}
				ansobj.put("sparql", spqarr);
			}

	        baseRequest.setHandled(true);

			JSONObject quobj = new JSONObject();
			quobj.put("question", question);
			quobj.put("answers", ansobj);

			double elaps = System.currentTimeMillis() - start_time;
			quobj.put("elaps", elaps / 1000.);
			quobj.put("status", 200);

			response.getWriter().println(quobj.toString());
		}
		catch(Exception e){
			if(e instanceof IOException){
				try {
					baseRequest.setHandled(true);
					response.getWriter().println(errorHandle("500","IOException",question,qlog));
				} catch (Exception e1) {
				}
			}
			else if(e instanceof JSONException){
				try {
					baseRequest.setHandled(true);
					response.getWriter().println(errorHandle("500","JSONException",question,qlog));
				} catch (Exception e1) {
				}
			}
			else if(e instanceof ServletException){
				try {
					baseRequest.setHandled(true);
					response.getWriter().println(errorHandle("500","ServletException",question,qlog));
				} catch (Exception e1) {
				}
			}
			else {
				try {
					baseRequest.setHandled(true);
					response.getWriter().println(errorHandle("500","Unkown Exception",question,qlog));
				} catch (Exception e1) {
				}
			}
		}
    }
}
