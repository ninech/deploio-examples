import Image from 'next/image'

var html = `
<style>
body {
  background-color: #141d50;
  color: #ffffff;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI',
   'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans',
   'Droid Sans', 'Helvetica Neue', 'Segoe UI Emoji',
   'Apple Color Emoji', 'Noto Color Emoji', sans-serif;
}
.logo {
  filter: invert(1);
  width: 30%;
  display: block;
  margin-left: auto;
  margin-right: auto;
}
</style>

<body>
  <h1 style="text-align:center">Next.js App</h1>
  <img class="logo" src="https://docs.nine.ch/img/theme/deploio.svg"></img>
</body>
`

export default function Home() {
  return (
    // yes this is ugly but also we just want to output our html for demo purposes
    <div dangerouslySetInnerHTML={{__html: html}} />
  )
}
