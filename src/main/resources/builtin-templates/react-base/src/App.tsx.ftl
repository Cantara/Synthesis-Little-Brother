function App() {
  return (
    <div>
      <h1>{{projectName | pascal-case}}</h1>
      <p>Welcome to {{projectName | pascal-case}}.</p>
    </div>
  )
}

export default App
