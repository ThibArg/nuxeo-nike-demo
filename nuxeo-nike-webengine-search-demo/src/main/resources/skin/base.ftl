<!DOCTYPE html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
  <title>
     <@block name="title">
     WebEngine Project
     </@block>
  </title>
  <link rel="stylesheet" href="${skinPath}/css/site.css" type="text/css" media="screen" charset="utf-8">
  <script src="${skinPath}/scripts/jquery-2.1.1.js"></script>
  <script src="${skinPath}/scripts/nuxeo.js"></script>
  <@block name="stylesheets" />
  <@block name="header_scripts" />
</head>

<body style="margin:0px 0px 0px 0px;">

  <table class="main">
    <tr>
      <td>
        <table class="header">
            <tr>
            <td width="100px"><img src="${skinPath}/img/logo.png"</td>
            <td><span class="bigWHite">&nbspSearch</span></td>
            <td align="right"></td>
            </tr>
        </table>
      </td>
    </tr>
    <tr height="98%">
      <td valign="top"><@block name="content">The Content</@block></td>
    </tr>
  </table/>

</body>
</html>
